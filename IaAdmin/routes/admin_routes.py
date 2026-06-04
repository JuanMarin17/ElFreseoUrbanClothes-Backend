from fastapi import APIRouter, Depends, Header, HTTPException
from sqlalchemy.orm import Session
from models.database import get_db, AdminChatMessage, AdminChatSession
from schemas.admin_schemas import (
    AdminChatRequest, AdminChatResponse,
    ImageAnalyzeRequest, ChatMessageResponse
)
from service.admin_chat_service import process_admin_chat
from service.ai_service import analyze_product_image
from service.image_processor import enhance_image
from typing import List
from uuid import UUID

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


def get_user_role(x_user_role: str = Header(None)) -> str:
    return x_user_role or ""


# ─── Chat ────────────────────────────────────────────────────────────────────

@router.post("/chat", response_model=AdminChatResponse)
async def admin_chat(
    dto: AdminChatRequest,
    admin_id: str  = Depends(get_admin_id),
    store_id: str  = Depends(get_store_id),
    jwt_token: str = Depends(get_jwt),
    role: str      = Depends(get_user_role),
    db: Session    = Depends(get_db)
):
    """
    Chat del agente admin/owner.
    Requiere headers: X-User-Id, X-Store-Id, Authorization, X-User-Role (ADMIN u OWNER).
    Capacidades: reportes, precios, estilo de tienda, análisis de imágenes,
    sugerencias de productos, alertas de inventario, soporte, promociones.
    """
    if role not in ("ADMIN", "OWNER"):
        raise HTTPException(status_code=403, detail="Solo ADMIN y OWNER pueden usar este agente")
    return await process_admin_chat(dto, admin_id, store_id, jwt_token, db)


# ─── Historial ───────────────────────────────────────────────────────────────

@router.get("/sessions", response_model=List[UUID])
async def get_sessions(
    admin_id: str = Depends(get_admin_id),
    db: Session   = Depends(get_db)
):
    """Lista sesiones del admin ordenadas por más reciente."""
    sessions = db.query(AdminChatSession).filter(
        AdminChatSession.admin_id == UUID(admin_id)
    ).order_by(AdminChatSession.created_at.desc()).all()
    return [s.session_id for s in sessions]


@router.get("/sessions/{session_id}/history", response_model=List[ChatMessageResponse])
async def get_history(
    session_id: UUID,
    db: Session = Depends(get_db)
):
    """Historial de mensajes de una sesión."""
    messages = db.query(AdminChatMessage).filter(
        AdminChatMessage.session_id == session_id
    ).order_by(AdminChatMessage.created_at.asc()).all()
    return messages


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
        session.session_id, ai_response, dummy_dto, store_id, ""
    )
