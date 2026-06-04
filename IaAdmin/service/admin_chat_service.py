import uuid
import json
from uuid import UUID
from sqlalchemy.orm import Session

from models.database import AdminChatSession, AdminChatMessage
from service.ai_service import generate_admin_response, analyze_product_image, build_context
from service.image_processor import enhance_image
from service.client import (
    reports_client, inventory_client, support_client,
    store_client, product_client, promotions_client
)
from schemas.admin_schemas import AdminChatRequest, AdminChatResponse

CONTEXT_WINDOW = 20  # últimos N mensajes enviados al modelo


def _extract_action_data(response: str) -> dict:
    data = {}
    try:
        action_part = response[response.index("ACTION:"):]
        parts = action_part.split("|")
        for part in parts:
            if ":" in part and not part.startswith("ACTION"):
                k, v = part.split(":", 1)
                k = k.strip()
                v = v.strip()
                try:
                    v = json.loads(v)
                except Exception:
                    pass
                data[k] = v
    except Exception as e:
        print(f"[admin] Error extrayendo action data: {e}")
    return data


def _clean_response(response: str) -> str:
    if "ACTION:" in response:
        return response[:response.index("ACTION:")].strip()
    return response.strip()


async def process_admin_chat(
    dto: AdminChatRequest,
    admin_id: str,
    store_id: str,
    jwt_token: str,
    db: Session
) -> AdminChatResponse:

    # Sesión
    if dto.session_id:
        session = db.query(AdminChatSession).filter(
            AdminChatSession.session_id == dto.session_id
        ).first()
        if not session:
            raise ValueError("Sesión no encontrada")
    else:
        session = AdminChatSession(
            admin_id=uuid.UUID(admin_id),
            store_id=uuid.UUID(store_id)
        )
        db.add(session)
        db.commit()
        db.refresh(session)

    # Guardar mensaje
    db.add(AdminChatMessage(
        session_id=session.session_id, role="user", content=dto.message
    ))
    db.commit()

    # Historial (ventana deslizante)
    messages = db.query(AdminChatMessage).filter(
        AdminChatMessage.session_id == session.session_id
    ).order_by(AdminChatMessage.created_at.asc()).all()
    history = [{"role": m.role, "content": m.content} for m in messages[-CONTEXT_WINDOW:]]

    # Contexto de la tienda (en paralelo)
    store_info = await store_client.get_store_info(store_id)
    products   = await product_client.get_active_products(store_id, jwt_token)
    dashboard  = await reports_client.get_dashboard(store_id, jwt_token)
    context    = build_context(store_info, dashboard, products)

    # Llamada a IA
    if dto.image_base64:
        ai_response = analyze_product_image(
            dto.image_base64,
            dto.image_mime_type or "image/jpeg",
            dto.message
        )
    else:
        ai_response = generate_admin_response(history, context)

    # Guardar respuesta
    db.add(AdminChatMessage(
        session_id=session.session_id, role="assistant", content=ai_response
    ))
    db.commit()

    return await _process_admin_action(
        session.session_id, ai_response, dto,
        store_id, jwt_token
    )


async def _process_admin_action(
    session_id: UUID,
    ai_response: str,
    dto: AdminChatRequest,
    store_id: str,
    jwt_token: str
) -> AdminChatResponse:

    response = AdminChatResponse(session_id=session_id, message=ai_response)

    # ── Reportes ──────────────────────────────────────────────────────────────
    if "ACTION:REPORT_DASHBOARD" in ai_response:
        data = await reports_client.get_dashboard(store_id, jwt_token)
        response.action = "REPORT_DASHBOARD"
        response.action_data = data
        response.message = _clean_response(ai_response) or "Aquí tienes el resumen del dashboard de tu tienda."

    elif "ACTION:REPORT_SALES" in ai_response:
        raw = _extract_action_data(ai_response)
        days = int(raw.get("days", 30))
        data = await reports_client.get_sales(store_id, jwt_token, days)
        response.action = "REPORT_SALES"
        response.action_data = data
        response.message = _clean_response(ai_response) or f"Reporte de ventas de los últimos {days} días."

    elif "ACTION:REPORT_STOCK" in ai_response:
        data = await reports_client.get_stock_report(store_id, jwt_token)
        response.action = "REPORT_STOCK"
        response.action_data = data
        response.message = _clean_response(ai_response) or "Aquí tienes el reporte de stock de tu tienda."

    elif "ACTION:REPORT_ORDERS" in ai_response:
        raw = _extract_action_data(ai_response)
        days = int(raw.get("days", 30))
        data = await reports_client.get_orders_report(store_id, jwt_token, days)
        response.action = "REPORT_ORDERS"
        response.action_data = data
        response.message = _clean_response(ai_response) or f"Reporte de órdenes de los últimos {days} días."

    # ── Inventario ────────────────────────────────────────────────────────────
    elif "ACTION:INVENTORY_ALERT" in ai_response:
        balance = await inventory_client.get_balance(store_id, jwt_token)
        low     = [b for b in balance if b.get("quantity", 0) <= 5]
        out     = [b for b in balance if b.get("quantity", 0) == 0]
        response.action = "INVENTORY_ALERT"
        response.action_data = {"lowStock": low, "outOfStock": out, "total": len(balance)}
        response.message = _clean_response(ai_response) or (
            f"⚠️ Tienes {len(out)} variantes sin stock y {len(low)} con stock crítico (≤5 unidades)."
        )

    # ── Soporte ───────────────────────────────────────────────────────────────
    elif "ACTION:SUPPORT_SUMMARY" in ai_response:
        tickets = await support_client.get_all_tickets(store_id, jwt_token)
        open_t  = [t for t in tickets if t.get("status") not in ("CLOSED",)]
        response.action = "SUPPORT_SUMMARY"
        response.action_data = {"tickets": open_t, "total": len(tickets), "open": len(open_t)}
        response.message = _clean_response(ai_response) or (
            f"Tienes {len(open_t)} tickets abiertos de {len(tickets)} en total."
        )

    elif "ACTION:REPLY_TICKET" in ai_response:
        data = _extract_action_data(ai_response)
        ticket_id = data.get("ticketId", "")
        message   = data.get("message", "")
        result = await support_client.reply_ticket(ticket_id, message, jwt_token)
        response.action = "REPLY_TICKET"
        response.action_data = result
        response.message = "Respuesta enviada al ticket correctamente."

    elif "ACTION:CLOSE_TICKET" in ai_response:
        data = _extract_action_data(ai_response)
        result = await support_client.close_ticket(data.get("ticketId", ""), jwt_token)
        response.action = "CLOSE_TICKET"
        response.action_data = result
        response.message = "Ticket cerrado correctamente."

    # ── Precios ───────────────────────────────────────────────────────────────
    elif "ACTION:SUGGEST_PRICE" in ai_response:
        data = _extract_action_data(ai_response)
        response.action = "SUGGEST_PRICE"
        response.action_data = data
        response.message = _clean_response(ai_response)

    # ── Estilo de tienda ──────────────────────────────────────────────────────
    elif "ACTION:SUGGEST_STORE_STYLE" in ai_response:
        data = _extract_action_data(ai_response)
        response.action = "SUGGEST_STORE_STYLE"
        response.action_data = data
        response.message = _clean_response(ai_response)

    # ── Productos ─────────────────────────────────────────────────────────────
    elif "ACTION:SUGGEST_PRODUCT" in ai_response:
        data = _extract_action_data(ai_response)
        response.action = "SUGGEST_PRODUCT"
        response.action_data = data
        response.message = _clean_response(ai_response)

    # ── Análisis de imagen ────────────────────────────────────────────────────
    elif "ACTION:ANALYZE_IMAGE" in ai_response:
        data = _extract_action_data(ai_response)
        response.action = "ANALYZE_IMAGE"
        response.action_data = data
        response.message = _clean_response(ai_response)

        if dto.image_base64:
            try:
                remove_bg  = str(data.get("removeBackground", "false")).lower() == "true"
                brightness = float(data.get("brightness", 1.0))
                contrast   = float(data.get("contrast", 1.0))
                sharpness  = float(data.get("sharpness", 1.2))
                enhanced_b64, enhanced_mime = enhance_image(
                    dto.image_base64,
                    dto.image_mime_type or "image/jpeg",
                    remove_background=remove_bg,
                    brightness=brightness,
                    contrast=contrast,
                    sharpness=sharpness,
                )
                response.enhanced_image_base64     = enhanced_b64
                response.enhanced_image_mime_type  = enhanced_mime
            except Exception as e:
                response.message += f"\n\n No se pudo procesar la imagen: {e}"

    # ── Promociones ───────────────────────────────────────────────────────────
    elif "ACTION:SUGGEST_PROMOTION" in ai_response:
        data = _extract_action_data(ai_response)
        response.action = "SUGGEST_PROMOTION"
        response.action_data = data
        response.message = _clean_response(ai_response)

    else:
        response.message = ai_response.strip()

    return response
