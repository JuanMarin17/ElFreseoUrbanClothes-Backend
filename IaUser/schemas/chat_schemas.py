from pydantic import BaseModel
from typing import Optional, Dict, Any, List
from uuid import UUID
from datetime import datetime


class ChatRequest(BaseModel):
    session_id: Optional[UUID] = None   # None = nueva sesión
    message: str
    image_base64: Optional[str] = None
    image_mime_type: Optional[str] = None


class ChatResponse(BaseModel):
    session_id: UUID
    message: str
    action: Optional[str] = None
    # Acciones posibles:
    #   ADD_TO_CART       → action_data: {variantId}
    #   STOCK_NOTIFY      → action_data: {variantId}
    #   SUPPORT_TICKET    → action_data: {subject}
    #   ORDER_SUMMARY     → sin action_data (el frontend consulta el carrito)
    #   COMPARE           → action_data: {productIds: "id1,id2"}
    action_data: Optional[Dict[str, Any]] = None
    # product_recommendations: lista de productos completos (incluye "imageUrl" con
    # la imagen principal, normalizada tanto en SEARCH como en GET_RECOMMENDATIONS)
    product_recommendations: Optional[List[Dict[str, Any]]] = None


class ChatMessageResponse(BaseModel):
    message_id: UUID
    role: str
    content: str
    created_at: datetime

    class Config:
        from_attributes = True


class StockNotificationResponse(BaseModel):
    notification_id: UUID
    variant_id: UUID
    notified: bool
    created_at: datetime

    class Config:
        from_attributes = True
