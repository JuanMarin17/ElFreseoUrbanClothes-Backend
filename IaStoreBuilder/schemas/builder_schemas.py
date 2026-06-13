from pydantic import BaseModel
from typing import Optional, Dict, Any, List
from uuid import UUID
from datetime import datetime


class BuilderChatRequest(BaseModel):
    session_id: Optional[UUID] = None
    message: str


class BuilderChatResponse(BaseModel):
    session_id: UUID
    message: str
    action: Optional[str] = None
    # Acciones posibles (action + action_data):
    #
    # SUGERENCIAS (el frontend las aplica al wizard — no guardan solas)
    #   SUGGEST_BASIC        → action_data: {name, description}
    #   SUGGEST_STYLES       → action_data: {cardBg, colorBoton, colorTitulo, colorParrafo,
    #                                         cardBorderColor1, cardBorderColor2,
    #                                         cardBorderWidth, cardRadius}
    #   SUGGEST_COMPONENTS   → action_data: {banner:{title,font,color,bg},
    #                                         header:{logo,font,color,bg},
    #                                         footer:{text,font,color,bg}}
    #   SUGGEST_LAYOUT       → action_data: {layoutId, layoutTitle, layoutDescription}
    #   SUGGEST_LEGAL        → action_data: {legalName, idNumber}
    #   SUGGEST_PAYMENT      → action_data: {paymentMethod, shipping}
    action_data: Optional[Dict[str, Any]] = None


class ChatMessageResponse(BaseModel):
    message_id: UUID
    role: str
    content: str
    created_at: datetime

    class Config:
        from_attributes = True
