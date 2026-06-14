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
    # Acciones posibles por paso (action + action_data):
    #
    #   PASO 1  SUGGEST_PLAN       → {plan: GRATUITO|BASICO|PRO|PREMIUM}
    #   PASO 2  SUGGEST_BASIC      → {name, description}
    #   PASO 3  SUGGEST_LEGAL      → {legalName, idNumber}
    #   PASO 4  SUGGEST_PAYMENT    → {paymentMethod, shipping}
    #   PASO 5  SUGGEST_LAYOUT     → {layoutId, layoutTitle, layoutDescription}
    #   PASO 6  SUGGEST_STYLES     → {colorBoton, colorTitulo, colorParrafo, cardBg,
    #                                  cardBorderColor1, cardBorderColor2, cardBorderWidth,
    #                                  cardRadius, cardShadow, buttonRadius, titleFont, bodyFont}
    #   PASO 7  SUGGEST_COMPONENTS → {banner:{title,font,size,color,bg,image},
    #                                  header:{logo,items,font,size,color,bg},
    #                                  footer:{text,font,size,color,bg}}
    #   PASO 8  SUGGEST_WIDGETS    → {sidebar:{visible,bg,color,font,width,items,border,radius},
    #                                  searchbar:{visible,bg,color,placeholderColor,
    #                                             placeholder,border,radius,icon}}
    action_data: Optional[Dict[str, Any]] = None


class ChatMessageResponse(BaseModel):
    message_id: UUID
    role: str
    content: str
    created_at: datetime

    class Config:
        from_attributes = True
