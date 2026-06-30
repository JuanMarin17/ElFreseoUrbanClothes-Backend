import os
from datetime import datetime, timedelta, timezone
from uuid import UUID

from sqlalchemy.orm import Session

from models.database import AdminRateLimit

# Ventana fija configurable por variables de entorno — política de producto,
# ajustable sin tocar código. Por defecto: 30 peticiones por hora por admin_id.
RATE_LIMIT_MAX     = int(os.getenv("IA_ADMIN_RATE_LIMIT_MAX", "30"))
RATE_LIMIT_WINDOW_MINUTES = int(os.getenv("IA_ADMIN_RATE_LIMIT_WINDOW_MINUTES", "60"))


class RateLimitExceeded(Exception):
    def __init__(self, retry_after_seconds: int):
        self.retry_after_seconds = retry_after_seconds
        super().__init__("Límite de peticiones excedido")


def check_and_increment(db: Session, admin_id: str) -> None:
    """Límite por admin_id, no por session_id: abrir una conversación nueva no reinicia el contador."""
    admin_uuid = UUID(admin_id)
    now = datetime.now(timezone.utc)
    window = timedelta(minutes=RATE_LIMIT_WINDOW_MINUTES)

    record = db.query(AdminRateLimit).filter(AdminRateLimit.admin_id == admin_uuid).first()

    if record is None:
        db.add(AdminRateLimit(admin_id=admin_uuid, window_start=now, request_count=1))
        db.commit()
        return

    elapsed = now - record.window_start
    if elapsed >= window:
        record.window_start = now
        record.request_count = 1
        db.commit()
        return

    if record.request_count >= RATE_LIMIT_MAX:
        retry_after_seconds = max(int((window - elapsed).total_seconds()), 1)
        db.rollback()
        raise RateLimitExceeded(retry_after_seconds)

    record.request_count += 1
    db.commit()
