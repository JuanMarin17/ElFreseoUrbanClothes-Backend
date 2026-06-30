from sqlalchemy import create_engine, Column, String, DateTime, Text, Integer
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from sqlalchemy.dialects.postgresql import UUID
from datetime import datetime, timezone
import uuid
from config.settings import DATABASE_URL

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


class AdminChatSession(Base):
    __tablename__ = "admin_chat_session"
    session_id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    admin_id   = Column(UUID(as_uuid=True), nullable=False)
    store_id   = Column(UUID(as_uuid=True), nullable=False)
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))


class AdminChatMessage(Base):
    __tablename__ = "admin_chat_message"
    message_id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    session_id = Column(UUID(as_uuid=True), nullable=False)
    role       = Column(String, nullable=False)
    content    = Column(Text, nullable=False)
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))


class AdminRateLimit(Base):
    """Contador de peticiones por admin_id (no por session_id) para limitar el uso del chat IA."""
    __tablename__ = "admin_rate_limit"
    admin_id      = Column(UUID(as_uuid=True), primary_key=True)
    window_start  = Column(DateTime(timezone=True), nullable=False)
    request_count = Column(Integer, nullable=False, default=0)


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def create_tables():
    Base.metadata.create_all(bind=engine)
