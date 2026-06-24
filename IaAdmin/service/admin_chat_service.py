import uuid
import json
import asyncio
import re
from uuid import UUID
from sqlalchemy.orm import Session

from models.database import AdminChatSession, AdminChatMessage
from service.ai_service import generate_admin_response, analyze_product_image, build_context
from service.image_processor import enhance_image
from service.image_generator import generate_image
import base64 as b64lib
from service.client import (
    reports_client, inventory_client, support_client,
    store_client, product_client, promotions_client, loyalty_client
)
from schemas.admin_schemas import AdminChatRequest, AdminChatResponse
from service.circuit_breaker import CircuitBreaker

CONTEXT_WINDOW = 20

# Instancias compartidas entre requests — rastrean fallos por servicio
_cb_reports = CircuitBreaker("reports", failure_threshold=3, reset_timeout=30)


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
            AdminChatSession.session_id == dto.session_id,
            AdminChatSession.admin_id  == uuid.UUID(admin_id),
            AdminChatSession.store_id  == uuid.UUID(store_id)
        ).first()
        if not session:
            from fastapi import HTTPException
            raise HTTPException(status_code=404, detail="Sesión no encontrada")
    else:
        session = AdminChatSession(
            admin_id=uuid.UUID(admin_id),
            store_id=uuid.UUID(store_id)
        )
        db.add(session)
        db.commit()
        db.refresh(session)
        print(f"[ia-admin][POST /chat] sesión creada session_id={session.session_id} admin_id={admin_id} store_id={store_id}")

    # Guardar mensaje del usuario
    db.add(AdminChatMessage(
        session_id=session.session_id, role="user", content=dto.message
    ))
    db.commit()

    # Historial (ventana deslizante)
    messages = db.query(AdminChatMessage).filter(
        AdminChatMessage.session_id == session.session_id
    ).order_by(AdminChatMessage.created_at.asc()).all()
    history = [{"role": m.role, "content": m.content} for m in messages[-CONTEXT_WINDOW:]]

    # Contexto completo de la tienda (todo en paralelo)
    results = await asyncio.gather(
        store_client.get_store_info(store_id),
        product_client.get_active_products(store_id, jwt_token),
        _cb_reports.call(reports_client.get_dashboard(store_id, jwt_token)),
        inventory_client.get_balance(store_id, jwt_token),
        promotions_client.get_active_promotions(store_id, jwt_token),
        support_client.get_all_tickets(store_id, jwt_token),
        return_exceptions=True,
    )
    store_info  = results[0] if isinstance(results[0], dict)  else {}
    products    = results[1] if isinstance(results[1], list)  else []
    dashboard   = results[2] if isinstance(results[2], dict)  else {}
    inventory   = results[3] if isinstance(results[3], list)  else []
    promotions  = results[4] if isinstance(results[4], list)  else []
    tickets     = results[5] if isinstance(results[5], list)  else []
    context = build_context(store_info, dashboard, products, inventory, promotions, tickets)

    # Llamada a IA
    if dto.image_base64:
        try:
            ai_response = analyze_product_image(
                dto.image_base64,
                dto.image_mime_type or "image/jpeg",
                dto.message
            )
        except RuntimeError as e:
            return AdminChatResponse(
                session_id=session.session_id,
                message=str(e)
            )
    else:
        ai_response = generate_admin_response(history, context)

    # Guardar respuesta limpia (sin ACTION tags)
    db.add(AdminChatMessage(
        session_id=session.session_id, role="assistant", content=_clean_response(ai_response)
    ))
    db.commit()

    return await _process_admin_action(
        session.session_id, ai_response, dto,
        admin_id, store_id, jwt_token
    )


async def _process_admin_action(
    session_id: UUID,
    ai_response: str,
    dto: AdminChatRequest,
    admin_id: str,
    store_id: str,
    jwt_token: str
) -> AdminChatResponse:

    # Normalizar espacios entre "ACTION:" y el nombre de la acción (ej: "ACTION: REPORT" → "ACTION:REPORT")
    ai_response = re.sub(r'ACTION:\s+', 'ACTION:', ai_response)

    response = AdminChatResponse(session_id=session_id, message=ai_response)

    # ── Reportes (ACTION:REPORT|type:sales/dashboard/stock/orders) ────────────
    if "ACTION:REPORT" in ai_response:
        raw  = _extract_action_data(ai_response)
        rtype = str(raw.get("type", "dashboard")).lower()
        days  = int(raw.get("days", 30))

        if rtype == "sales":
            data = await reports_client.get_sales(store_id, jwt_token, days)
            response.action = "REPORT_SALES"
            response.action_data = data
            response.message = _clean_response(ai_response) or f"Reporte de ventas de los últimos {days} días."

        elif rtype == "stock":
            data = await reports_client.get_stock_report(store_id, jwt_token)
            response.action = "REPORT_STOCK"
            response.action_data = data
            response.message = _clean_response(ai_response) or "Aquí tienes el reporte de stock de tu tienda."

        elif rtype == "orders":
            data = await reports_client.get_orders_report(store_id, jwt_token, days)
            response.action = "REPORT_ORDERS"
            response.action_data = data
            response.message = _clean_response(ai_response) or f"Reporte de órdenes de los últimos {days} días."

        else:  # dashboard (por defecto)
            data = await reports_client.get_dashboard(store_id, jwt_token)
            response.action = "REPORT_DASHBOARD"
            response.action_data = data
            response.message = _clean_response(ai_response) or "Aquí tienes el resumen del dashboard de tu tienda."

    # ── Sugerencias de tienda (ACTION:STORE_SUGGESTION|type:colors/typography/layout/branding) ──
    elif "ACTION:STORE_SUGGESTION" in ai_response:
        raw   = _extract_action_data(ai_response)
        stype = str(raw.get("type", "colors")).lower()
        response.action = f"STORE_SUGGESTION_{stype.upper()}"
        response.action_data = raw
        response.message = _clean_response(ai_response)

    # ── Sugerencia de precio (ACTION:PRICE_SUGGESTION) ────────────────────────
    elif "ACTION:PRICE_SUGGESTION" in ai_response:
        data = _extract_action_data(ai_response)
        response.action = "PRICE_SUGGESTION"
        response.action_data = data
        response.message = _clean_response(ai_response)

    # ── Inventario (ACTION:INVENTORY_ALERT) ───────────────────────────────────
    elif "ACTION:INVENTORY_ALERT" in ai_response:
        balance = await inventory_client.get_balance(store_id, jwt_token)
        low     = [b for b in balance if 0 < b.get("quantity", 0) <= 5]
        out     = [b for b in balance if b.get("quantity", 0) == 0]
        response.action = "INVENTORY_ALERT"
        response.action_data = {
            "lowStock": low,
            "outOfStock": out,
            "totalVariants": len(balance),
            "criticalCount": len(low),
            "outOfStockCount": len(out),
        }
        response.message = _clean_response(ai_response) or (
            f"Tienes {len(out)} variantes sin stock y {len(low)} con stock crítico (1-5 unidades)."
        )

    # ── Lealtad (ACTION:LOYALTY_SUMMARY) ─────────────────────────────────────
    elif "ACTION:LOYALTY_SUMMARY" in ai_response:
        ledger = await loyalty_client.get_ledger(store_id, admin_id, jwt_token)

        total_earned   = sum(e.get("points", 0) for e in ledger if e.get("type") == "EARN")
        total_redeemed = abs(sum(e.get("points", 0) for e in ledger if e.get("type") == "REDEEM"))
        total_expired  = abs(sum(e.get("points", 0) for e in ledger if e.get("type") == "EXPIRE"))

        response.action = "LOYALTY_SUMMARY"
        response.action_data = {
            "totalEarned": total_earned,
            "totalRedeemed": total_redeemed,
            "totalExpired": total_expired,
            "transactions": ledger,
            "note": (
                "Los datos corresponden a la cuenta del administrador. "
                "Para estadísticas globales de todos los usuarios se requiere un endpoint de administración adicional."
            ),
        }
        response.message = _clean_response(ai_response) or (
            f"Resumen de lealtad: {total_earned} puntos ganados, "
            f"{total_redeemed} canjeados, {total_expired} vencidos."
        )

    # ── Soporte (ACTION:SUPPORT_SUMMARY) ──────────────────────────────────────
    elif "ACTION:SUPPORT_SUMMARY" in ai_response:
        tickets = await support_client.get_all_tickets(store_id, jwt_token)
        open_t  = [t for t in tickets if t.get("status") not in ("CLOSED",)]
        response.action = "SUPPORT_SUMMARY"
        response.action_data = {
            "tickets": open_t,
            "total": len(tickets),
            "open": len(open_t),
        }
        response.message = _clean_response(ai_response) or (
            f"Tienes {len(open_t)} tickets abiertos de {len(tickets)} en total."
        )

    elif "ACTION:REPLY_TICKET" in ai_response:
        data      = _extract_action_data(ai_response)
        ticket_id = data.get("ticketId", "")
        message   = data.get("message", "")
        result    = await support_client.reply_ticket(ticket_id, message, jwt_token)
        response.action = "REPLY_TICKET"
        response.action_data = result
        response.message = "Respuesta enviada al ticket correctamente."

    elif "ACTION:CLOSE_TICKET" in ai_response:
        data   = _extract_action_data(ai_response)
        result = await support_client.close_ticket(data.get("ticketId", ""), jwt_token)
        response.action = "CLOSE_TICKET"
        response.action_data = result
        response.message = "Ticket cerrado correctamente."

    # ── Promociones (ACTION:SUGGEST_PROMOTION) ────────────────────────────────
    elif "ACTION:SUGGEST_PROMOTION" in ai_response:
        data = _extract_action_data(ai_response)
        response.action = "SUGGEST_PROMOTION"
        response.action_data = data
        response.message = _clean_response(ai_response)

    # ── Sugerencia de producto (ACTION:SUGGEST_PRODUCT) ───────────────────────
    elif "ACTION:SUGGEST_PRODUCT" in ai_response:
        data = _extract_action_data(ai_response)
        response.action = "SUGGEST_PRODUCT"
        response.action_data = data
        response.message = _clean_response(ai_response)

    # ── Análisis de imagen (ACTION:ANALYZE_IMAGE) ─────────────────────────────
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
                response.enhanced_image_base64    = enhanced_b64
                response.enhanced_image_mime_type = enhanced_mime
            except Exception as e:
                response.message += f"\n\nNo se pudo procesar la imagen: {e}"

    # ── Generación de imagen (ACTION:GENERATE_IMAGE) ──────────────────────────
    elif "ACTION:GENERATE_IMAGE" in ai_response:
        data = _extract_action_data(ai_response)
        prompt = str(data.get("prompt", "")).strip()
        aspect_ratio = str(data.get("aspectRatio", "1:1"))
        response.action = "GENERATE_IMAGE"
        response.action_data = {"prompt": prompt, "aspectRatio": aspect_ratio}
        response.message = _clean_response(ai_response) or "Aquí tienes la imagen generada."

        try:
            image_bytes = generate_image(prompt, aspect_ratio)
            response.generated_image_base64 = b64lib.b64encode(image_bytes).decode("utf-8")
            response.generated_image_mime_type = "image/png"
        except Exception as e:
            response.message += f"\n\nNo se pudo generar la imagen: {e}"

    # ── Generación de archivo de reporte (ACTION:GENERATE_REPORT) ────────────
    elif "ACTION:GENERATE_REPORT" in ai_response:
        data = _extract_action_data(ai_response)
        fmt   = str(data.get("format", "excel")).lower()
        rtype = str(data.get("type", "sales")).lower()
        days  = int(data.get("days", 30))

        if rtype == "sales":
            report_data = await reports_client.get_sales(store_id, jwt_token, days)
        elif rtype == "stock":
            report_data = await reports_client.get_stock_report(store_id, jwt_token)
        elif rtype == "orders":
            report_data = await reports_client.get_orders_report(store_id, jwt_token, days)
        else:
            report_data = await reports_client.get_dashboard(store_id, jwt_token)

        try:
            from service.report_generator import generate_report
            file_bytes, mime_type, filename = generate_report(report_data, rtype, fmt, days)
            response.action       = "GENERATE_REPORT"
            response.action_data  = {"format": fmt, "type": rtype, "days": days}
            response.report_base64    = b64lib.b64encode(file_bytes).decode("utf-8")
            response.report_mime_type = mime_type
            response.report_filename  = filename
            fmt_label = {"excel": "Excel", "pdf": "PDF", "chart": "gráfica"}.get(fmt, fmt)
            response.message = _clean_response(ai_response) or f"Aquí tienes el reporte de {rtype} en formato {fmt_label}."
        except Exception as e:
            response.message = f"No se pudo generar el archivo: {e}"

    else:
        response.message = _clean_response(ai_response)

    return response
