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
    # Acciones posibles (action + action_data):
    #
    # REPORTES
    #   REPORT_DASHBOARD     → action_data: datos del dashboard general
    #   REPORT_SALES         → action_data: ventas por período, ingresos COP, top productos
    #   REPORT_STOCK         → action_data: estado del inventario y alertas de stock
    #   REPORT_ORDERS        → action_data: órdenes por estado, comparativa de período
    #
    # PRECIOS
    #   PRICE_SUGGESTION     → action_data: {productId, suggestedPrice, reason}
    #
    # ESTILO DE TIENDA (generado por IA, solo sugerencias)
    #   STORE_SUGGESTION_COLORS      → action_data: {primaryColor, secondaryColor, accentColor, ...}
    #   STORE_SUGGESTION_TYPOGRAPHY  → action_data: {headingFont, bodyFont, ...} (Google Fonts)
    #   STORE_SUGGESTION_LAYOUT      → action_data: descripción del layout sugerido
    #   STORE_SUGGESTION_BRANDING    → action_data: {slogan, shortDescription, longDescription}
    #
    # PRODUCTOS
    #   SUGGEST_PRODUCT      → action_data: {name, description, price}
    #
    # IMAGEN DE PRODUCTO
    #   ANALYZE_IMAGE        → action_data: {removeBackground, brightness, contrast, sharpness}
    #                          + enhanced_image_base64 si se procesó
    #
    # INVENTARIO
    #   INVENTORY_ALERT      → action_data: {lowStock: [], outOfStock: [], totalVariants, criticalCount}
    #
    # LEALTAD
    #   LOYALTY_SUMMARY      → action_data: {totalEarned, totalRedeemed, totalExpired, transactions, note}
    #
    # SOPORTE
    #   SUPPORT_SUMMARY      → action_data: {tickets: [], total, open}
    #   REPLY_TICKET         → action_data: resultado de la respuesta enviada
    #   CLOSE_TICKET         → action_data: resultado del cierre
    #
    # PROMOCIONES
    #   SUGGEST_PROMOTION    → action_data: {type, discount, duration, target, reason}
    #
    # GENERACIÓN DE IMAGEN
    #   GENERATE_IMAGE       → action_data: {prompt, aspectRatio}
    #                          + generated_image_base64 si se generó
    action_data: Optional[Dict[str, Any]] = None
    enhanced_image_base64: Optional[str] = None
    enhanced_image_mime_type: Optional[str] = None
    generated_image_base64: Optional[str] = None
    generated_image_mime_type: Optional[str] = None
    report_base64: Optional[str] = None
    report_mime_type: Optional[str] = None
    report_filename: Optional[str] = None


class ImageAnalyzeRequest(BaseModel):
    """Análisis de imagen de producto sin chat"""
    image_base64: str
    mime_type: str = "image/jpeg"
    context: Optional[str] = None


class ProductSuggestRequest(BaseModel):
    """Generador de ficha de producto (nombre/descripción/precio/categoría) sin chat."""
    hint: Optional[str] = None
    image_base64: Optional[str] = None
    image_mime_type: Optional[str] = None
    existing_categories: Optional[List[str]] = None


class ProductSuggestResponse(BaseModel):
    name: Optional[str] = None
    description: Optional[str] = None
    price: Optional[int] = None
    category: Optional[str] = None


class ChatMessageResponse(BaseModel):
    message_id: UUID
    role: str
    content: str
    created_at: datetime

    class Config:
        from_attributes = True
