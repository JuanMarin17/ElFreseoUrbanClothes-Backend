from fastapi import APIRouter, Depends, Header, HTTPException
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session
from models.database import get_db, AdminChatMessage, AdminChatSession
from schemas.admin_schemas import (
    AdminChatRequest, AdminChatResponse,
    ImageAnalyzeRequest, ChatMessageResponse
)
from service.admin_chat_service import process_admin_chat
from service.ai_service import analyze_product_image
from service.image_processor import enhance_image
from service.image_generator import generate_image
from service.client import store_client
from pydantic import BaseModel
from typing import Optional, List
from uuid import UUID
import base64


class ImageGenerateRequest(BaseModel):
    prompt: str
    aspect_ratio: Optional[str] = "1:1"

router = APIRouter(prefix="/api/v1/ia/admin", tags=["IA Admin"])


def get_admin_id(x_user_id: str = Header(None)) -> str:
    if not x_user_id:
        raise HTTPException(status_code=401, detail="Admin no autenticado")
    return x_user_id


def get_store_id(x_store_id: str = Header(None)) -> str:
    if not x_store_id:
        raise HTTPException(status_code=400, detail="No se encontró X-Store-Id")
    return x_store_id


def get_jwt(authorization: str = Header(None)) -> str:
    if not authorization:
        return ""
    return authorization.replace("Bearer ", "").strip()


# ─── Chat ────────────────────────────────────────────────────────────────────

@router.post("/chat", response_model=AdminChatResponse)
async def admin_chat(
    dto: AdminChatRequest,
    admin_id: str  = Depends(get_admin_id),
    store_id: str  = Depends(get_store_id),
    jwt_token: str = Depends(get_jwt),
    db: Session    = Depends(get_db)
):
    """
    Chat del agente admin/owner.
    Requiere headers: X-User-Id, X-Store-Id, Authorization.
    El rol se valida consultando el módulo Store — debe ser OWNER o ADMIN.
    Capacidades: reportes, precios, estilo de tienda, análisis de imágenes,
    sugerencias de productos, alertas de inventario, soporte, promociones.
    """
    role = await store_client.get_user_store_role(store_id, admin_id)
    if role not in ("ADMIN", "OWNER"):
        raise HTTPException(
            status_code=403,
            detail="Acceso denegado: el usuario no es ADMIN ni OWNER de esta tienda"
        )
    return await process_admin_chat(dto, admin_id, store_id, jwt_token, db)


# ─── Historial ───────────────────────────────────────────────────────────────

@router.get("/sessions", response_model=List[UUID])
async def get_sessions(
    admin_id: str = Depends(get_admin_id),
    store_id: str = Depends(get_store_id),
    db: Session   = Depends(get_db)
):
    """Lista sesiones del admin para la tienda actual, ordenadas por más reciente."""
    sessions = db.query(AdminChatSession).filter(
        AdminChatSession.admin_id == UUID(admin_id),
        AdminChatSession.store_id == UUID(store_id)
    ).order_by(AdminChatSession.created_at.desc()).all()
    return [s.session_id for s in sessions]


@router.get("/sessions/{session_id}/history", response_model=List[ChatMessageResponse])
async def get_history(
    session_id: UUID,
    admin_id: str = Depends(get_admin_id),
    store_id: str = Depends(get_store_id),
    db: Session   = Depends(get_db)
):
    """Historial de mensajes de una sesión, validando que pertenezca a la tienda actual."""
    session = db.query(AdminChatSession).filter(
        AdminChatSession.session_id == session_id,
        AdminChatSession.admin_id  == UUID(admin_id),
        AdminChatSession.store_id  == UUID(store_id)
    ).first()
    if not session:
        raise HTTPException(status_code=404, detail="Sesión no encontrada")

    messages = db.query(AdminChatMessage).filter(
        AdminChatMessage.session_id == session_id
    ).order_by(AdminChatMessage.created_at.asc()).all()
    return messages


# ─── Borrar sesiones ─────────────────────────────────────────────────────────

@router.delete("/sessions/{session_id}", status_code=204)
async def delete_session(
    session_id: UUID,
    admin_id: str = Depends(get_admin_id),
    store_id: str = Depends(get_store_id),
    db: Session   = Depends(get_db)
):
    """Elimina una sesión de chat y todos sus mensajes."""
    session = db.query(AdminChatSession).filter(
        AdminChatSession.session_id == session_id,
        AdminChatSession.admin_id  == UUID(admin_id),
        AdminChatSession.store_id  == UUID(store_id)
    ).first()
    if not session:
        raise HTTPException(status_code=404, detail="Sesión no encontrada")
    db.query(AdminChatMessage).filter(AdminChatMessage.session_id == session_id).delete(synchronize_session=False)
    db.delete(session)
    db.commit()


@router.delete("/sessions", status_code=204)
async def delete_all_sessions(
    admin_id: str = Depends(get_admin_id),
    store_id: str = Depends(get_store_id),
    db: Session   = Depends(get_db)
):
    """Elimina todas las sesiones de chat del admin en esta tienda y sus mensajes."""
    session_ids = [
        s.session_id for s in
        db.query(AdminChatSession).filter(
            AdminChatSession.admin_id == UUID(admin_id),
            AdminChatSession.store_id == UUID(store_id)
        ).all()
    ]
    if session_ids:
        db.query(AdminChatMessage).filter(AdminChatMessage.session_id.in_(session_ids)).delete(synchronize_session=False)
        db.query(AdminChatSession).filter(
            AdminChatSession.admin_id == UUID(admin_id),
            AdminChatSession.store_id == UUID(store_id)
        ).delete(synchronize_session=False)
        db.commit()


# ─── Análisis de imagen directo (sin chat) ───────────────────────────────────

@router.post("/image/analyze", response_model=AdminChatResponse)
async def analyze_image(
    dto: ImageAnalyzeRequest,
    admin_id: str = Depends(get_admin_id),
    store_id: str = Depends(get_store_id),
    db: Session   = Depends(get_db)
):
    """
    Analiza una imagen de producto directamente con Gemini Vision.
    Devuelve sugerencias + imagen mejorada en base64.
    """
    role = await store_client.get_user_store_role(store_id, admin_id)
    if role not in ("ADMIN", "OWNER"):
        raise HTTPException(
            status_code=403,
            detail="Acceso denegado: el usuario no es ADMIN ni OWNER de esta tienda"
        )

    ai_response = analyze_product_image(
        dto.image_base64, dto.mime_type, dto.context or ""
    )

    # Crear sesión temporal para guardar el análisis
    from models.database import AdminChatSession, AdminChatMessage
    import uuid as uuid_lib
    session = AdminChatSession(
        admin_id=UUID(admin_id),
        store_id=UUID(store_id)
    )
    db.add(session)
    db.commit()
    db.refresh(session)
    db.add(AdminChatMessage(
        session_id=session.session_id, role="assistant", content=ai_response
    ))
    db.commit()

    from service.admin_chat_service import _process_admin_action
    from schemas.admin_schemas import AdminChatRequest
    dummy_dto = AdminChatRequest(
        message="",
        image_base64=dto.image_base64,
        image_mime_type=dto.mime_type
    )
    return await _process_admin_action(
        session.session_id, ai_response, dummy_dto, admin_id, store_id, ""
    )


# ─── Generación de imágenes ───────────────────────────────────────────────────

@router.post("/generate-image")
async def generate_product_image(
    dto: ImageGenerateRequest,
    admin_id: str = Depends(get_admin_id),
    store_id: str = Depends(get_store_id)
):
    """
    Genera una imagen de producto usando Imagen 3 de Google.
    aspect_ratio: "1:1" | "4:3" | "16:9" | "9:16"
    Devuelve la imagen en base64.
    """
    role = await store_client.get_user_store_role(store_id, admin_id)
    if role not in ("ADMIN", "OWNER"):
        raise HTTPException(
            status_code=403,
            detail="Acceso denegado: el usuario no es ADMIN ni OWNER de esta tienda"
        )

    image_bytes = generate_image(dto.prompt, dto.aspect_ratio)
    return JSONResponse({
        "image_base64": base64.b64encode(image_bytes).decode("utf-8"),
        "mime_type": "image/png"
    })
