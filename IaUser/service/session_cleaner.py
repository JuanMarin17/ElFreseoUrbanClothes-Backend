"""
Limpieza automática de sesiones viejas.
Se ejecuta al iniciar la app y cada 24h en background.
- Archiva sesiones con más de 30 días sin actividad
- Limita a MAX_MESSAGES_PER_SESSION mensajes por sesión (elimina los más viejos)
"""
import asyncio
from datetime import datetime, timezone, timedelta
from sqlalchemy.orm import Session
from models.database import SessionLocal, ChatSession, ChatMessage

MAX_MESSAGES_PER_SESSION = 50   # máximo de mensajes por sesión
SESSION_TTL_DAYS = 30           # días de inactividad antes de archivar
CLEAN_INTERVAL_HOURS = 24       # cada cuántas horas limpiar


def clean_old_sessions(db: Session):
    cutoff = datetime.now(timezone.utc) - timedelta(days=SESSION_TTL_DAYS)

    # Sesiones sin actividad reciente
    old_sessions = db.query(ChatSession).filter(
        ChatSession.created_at < cutoff
    ).all()

    deleted = 0
    for session in old_sessions:
        # Verificar si el último mensaje es también viejo
        last_msg = db.query(ChatMessage).filter(
            ChatMessage.session_id == session.session_id
        ).order_by(ChatMessage.created_at.desc()).first()

        if last_msg is None or last_msg.created_at.replace(tzinfo=timezone.utc) < cutoff:
            db.query(ChatMessage).filter(
                ChatMessage.session_id == session.session_id
            ).delete()
            db.delete(session)
            deleted += 1

    db.commit()
    print(f"[session_cleaner] {deleted} sesiones antiguas eliminadas")


def trim_session_messages(db: Session, session_id):
    """Si una sesión supera MAX_MESSAGES_PER_SESSION, elimina los más viejos."""
    messages = db.query(ChatMessage).filter(
        ChatMessage.session_id == session_id
    ).order_by(ChatMessage.created_at.asc()).all()

    if len(messages) > MAX_MESSAGES_PER_SESSION:
        to_delete = messages[:len(messages) - MAX_MESSAGES_PER_SESSION]
        for msg in to_delete:
            db.delete(msg)
        db.commit()


async def start_cleaner():
    """Loop asíncrono que limpia sesiones cada CLEAN_INTERVAL_HOURS horas."""
    while True:
        await asyncio.sleep(CLEAN_INTERVAL_HOURS * 3600)
        db = SessionLocal()
        try:
            clean_old_sessions(db)
        finally:
            db.close()
