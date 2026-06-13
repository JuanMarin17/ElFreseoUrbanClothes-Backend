from fastapi import APIRouter, Depends, Header, HTTPException
from sqlalchemy.orm import Session
from models.database import get_db, BuilderSession, BuilderMessage
from schemas.builder_schemas import BuilderChatRequest, BuilderChatResponse, ChatMessageResponse
from service.builder_chat_service import process_builder_chat
from service.client import store_client
from typing import List
from uuid import UUID

router = APIRouter(prefix="/api/v1/ia/builder", tags=["IA Store Builder"])


def get_owner_id(x_user_id: str = Header(None)) -> str:
    if not x_user_id:
        raise HTTPException(status_code=401, detail="Usuario no autenticado")
    return x_user_id


def get_store_id(x_store_id: str = Header(None)) -> str:
    if not x_store_id:
        raise HTTPException(status_code=400, detail="No se encontró X-Store-Id")
    return x_store_id


def get_jwt(authorization: str = Header(None)) -> str:
    if not authorization:
        return ""
    return authorization.replace("Bearer ", "").strip()


# ─── Chat principal ───────────────────────────────────────────────────────────

@router.post("/chat", response_model=BuilderChatResponse)
async def builder_chat(
    dto: BuilderChatRequest,
    owner_id: str  = Depends(get_owner_id),
    store_id: str  = Depends(get_store_id),
    jwt_token: str = Depends(get_jwt),
    db: Session    = Depends(get_db)
):
    """
    Chat del asistente de creación de tiendas.
    Requiere headers: X-User-Id, X-Store-Id, Authorization.
    Solo accesible para el OWNER de la tienda.
    Guía al dueño a través de 6 fases: identidad, estilos, componentes,
    layout, información legal y métodos de pago/envío.
    """
    role = await store_client.get_user_store_role(store_id, owner_id)
    if role != "OWNER":
        raise HTTPException(
            status_code=403,
            detail="Acceso denegado: solo el propietario (OWNER) puede usar el asistente de creación"
        )
    return await process_builder_chat(dto, owner_id, store_id, jwt_token, db)


# ─── Historial ────────────────────────────────────────────────────────────────

@router.get("/sessions", response_model=List[UUID])
async def get_sessions(
    owner_id: str = Depends(get_owner_id),
    db: Session   = Depends(get_db)
):
    """Lista sesiones del dueño ordenadas por más reciente."""
    sessions = db.query(BuilderSession).filter(
        BuilderSession.owner_id == UUID(owner_id)
    ).order_by(BuilderSession.created_at.desc()).all()
    return [s.session_id for s in sessions]


@router.get("/sessions/{session_id}/history", response_model=List[ChatMessageResponse])
async def get_history(
    session_id: UUID,
    db: Session = Depends(get_db)
):
    """Historial de mensajes de una sesión de configuración."""
    messages = db.query(BuilderMessage).filter(
        BuilderMessage.session_id == session_id
    ).order_by(BuilderMessage.created_at.asc()).all()
    return messages
