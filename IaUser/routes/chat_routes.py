from fastapi import APIRouter, Depends, Header, HTTPException
from sqlalchemy.orm import Session
from models.database import get_db, ChatMessage, ChatSession, StockNotification
from schemas.chat_schemas import (
    ChatRequest, ChatResponse, ChatMessageResponse, StockNotificationResponse
)
from service.chat_service import process_chat
from typing import List
from uuid import UUID

router = APIRouter(prefix="/api/v1/ia/user", tags=["IA User"])


def get_user_id(x_user_id: str = Header(None)) -> str:
    if not x_user_id:
        raise HTTPException(status_code=401, detail="Usuario no autenticado")
    return x_user_id


def get_store_id(x_store_id: str = Header(None)) -> str:
    if not x_store_id:
        raise HTTPException(status_code=400, detail="No se encontró X-Store-Id")
    return x_store_id


# ─── Chat ────────────────────────────────────────────────────────────────────

@router.post("/chat", response_model=ChatResponse)
async def chat(
    dto: ChatRequest,
    user_id: str = Depends(get_user_id),
    store_id: str = Depends(get_store_id),
    db: Session = Depends(get_db)
):
    """
    Envía un mensaje al asistente.
    - Si no viene session_id se crea una sesión nueva (nuevo historial).
    - El historial completo de la sesión se envía al modelo en cada turno.
    """
    return await process_chat(dto, user_id, store_id, db)


# ─── Sesiones ────────────────────────────────────────────────────────────────

@router.get("/sessions", response_model=List[UUID])
async def get_sessions(
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    """Lista las sesiones de chat del usuario (ordenadas por más reciente)."""
    sessions = db.query(ChatSession).filter(
        ChatSession.user_id == UUID(user_id)
    ).order_by(ChatSession.created_at.desc()).all()
    return [s.session_id for s in sessions]


@router.get("/sessions/{session_id}/history", response_model=List[ChatMessageResponse])
async def get_history(
    session_id: UUID,
    db: Session = Depends(get_db)
):
    """Devuelve el historial de mensajes de una sesión."""
    messages = db.query(ChatMessage).filter(
        ChatMessage.session_id == session_id
    ).order_by(ChatMessage.created_at.asc()).all()
    return messages


# ─── Notificaciones de stock ──────────────────────────────────────────────────

@router.get("/stock-notifications", response_model=List[StockNotificationResponse])
async def get_stock_notifications(
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    """Lista las alertas de stock activas del usuario (aún no notificadas)."""
    notifs = db.query(StockNotification).filter(
        StockNotification.user_id == UUID(user_id),
        StockNotification.notified == False
    ).all()
    return notifs


@router.delete("/stock-notifications/{notification_id}", status_code=204)
async def cancel_stock_notification(
    notification_id: UUID,
    user_id: str = Depends(get_user_id),
    db: Session = Depends(get_db)
):
    """Cancela una alerta de stock."""
    notif = db.query(StockNotification).filter(
        StockNotification.notification_id == notification_id,
        StockNotification.user_id == UUID(user_id)
    ).first()
    if not notif:
        raise HTTPException(status_code=404, detail="Notificación no encontrada")
    db.delete(notif)
    db.commit()
