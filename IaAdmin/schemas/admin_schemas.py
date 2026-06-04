from pydantic import BaseModel
from typing import Optional, Dict, Any, List
from uuid import UUID
from datetime import datetime


class AdminChatRequest(BaseModel):
    session_id: Optional[UUID] = None
    message: str
    image_base64: Optional[str] = None
    image_mime_type: Optional[str] = None


class AdminChatResponse(BaseModel):
    session_id: UUID
    message: str
    action: Optional[str] = None
    # Acciones posibles:
    #   REPORT_DASHBOARD     → action_data: {dashboard data interpretado}
    #   REPORT_SALES         → action_data: {sales data interpretado}
    #   REPORT_STOCK         → action_data: {stock data interpretado}
    #   REPORT_ORDERS        → action_data: {orders data interpretado}
    #   SUGGEST_PRICE        → action_data: {productId, suggestedPrice, reason}
    #   SUGGEST_STORE_STYLE  → action_data: {colors, fonts, borderRadius}
    #   SUGGEST_PRODUCT      → action_data: {name, description, price}
    #   ANALYZE_IMAGE        → action_data: {removeBackground, brightness, contrast, suggestions}
    #   INVENTORY_ALERT      → action_data: {lowStock: [], outOfStock: []}
    #   SUPPORT_SUMMARY      → action_data: {tickets: [], priority: []}
    #   SUGGEST_PROMOTION    → action_data: {type, discount, reason}
    action_data: Optional[Dict[str, Any]] = None
    enhanced_image_base64: Optional[str] = None
    enhanced_image_mime_type: Optional[str] = None


class ImageAnalyzeRequest(BaseModel):
    """Análisis de imagen de producto sin chat"""
    image_base64: str
    mime_type: str = "image/jpeg"
    context: Optional[str] = None


class ChatMessageResponse(BaseModel):
    message_id: UUID
    role: str
    content: str
    created_at: datetime

    class Config:
        from_attributes = True
