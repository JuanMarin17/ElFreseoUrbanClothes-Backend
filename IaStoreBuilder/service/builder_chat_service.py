import uuid
import json
import asyncio
import re
from uuid import UUID
from sqlalchemy.orm import Session

from models.database import BuilderSession, BuilderMessage
from service.ai_service import generate_builder_response, build_builder_context
from service.client import store_client
from schemas.builder_schemas import BuilderChatRequest, BuilderChatResponse

CONTEXT_WINDOW = 20


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
        print(f"[builder] Error extrayendo action data: {e}")
    return data


def _clean_response(response: str) -> str:
    if "ACTION:" in response:
        return response[:response.index("ACTION:")].strip()
    return response.strip()


async def process_builder_chat(
    dto: BuilderChatRequest,
    owner_id: str,
    store_id: str | None,
    jwt_token: str,
    db: Session
) -> BuilderChatResponse:

    # Sesión
    if dto.session_id:
        session = db.query(BuilderSession).filter(
            BuilderSession.session_id == dto.session_id
        ).first()
        if not session:
            raise ValueError("Sesión no encontrada")
    else:
        session = BuilderSession(
            owner_id=uuid.UUID(owner_id),
            store_id=uuid.UUID(store_id) if store_id else None
        )
        db.add(session)
        db.commit()
        db.refresh(session)

    # Guardar mensaje del usuario
    db.add(BuilderMessage(
        session_id=session.session_id, role="user", content=dto.message
    ))
    db.commit()

    # Historial (ventana deslizante)
    messages = db.query(BuilderMessage).filter(
        BuilderMessage.session_id == session.session_id
    ).order_by(BuilderMessage.created_at.asc()).all()
    history = [{"role": m.role, "content": m.content} for m in messages[-CONTEXT_WINDOW:]]

    # Contexto de la tienda (solo si ya existe)
    store_info, settings = {}, {}
    if store_id:
        results = await asyncio.gather(
            store_client.get_store_info(store_id),
            store_client.get_store_settings(store_id, jwt_token),
            return_exceptions=True,
        )
        store_info = results[0] if isinstance(results[0], dict) else {}
        settings   = results[1] if isinstance(results[1], dict) else {}
    context = build_builder_context(store_info, settings)

    # Llamada a IA
    ai_response = generate_builder_response(history, context)

    # Guardar respuesta limpia (sin ACTION tags)
    db.add(BuilderMessage(
        session_id=session.session_id, role="assistant", content=_clean_response(ai_response)
    ))
    db.commit()

    return _process_builder_action(session.session_id, ai_response)


def _process_builder_action(session_id: UUID, ai_response: str) -> BuilderChatResponse:
    # Normalizar espacios entre "ACTION:" y el nombre
    ai_response = re.sub(r'ACTION:\s+', 'ACTION:', ai_response)

    response = BuilderChatResponse(session_id=session_id, message=_clean_response(ai_response))

    # ── Identidad básica ──────────────────────────────────────────────────────
    if "ACTION:SUGGEST_BASIC" in ai_response:
        data = _extract_action_data(ai_response)
        response.action = "SUGGEST_BASIC"
        response.action_data = {
            "name":        data.get("name", ""),
            "description": data.get("description", ""),
        }

    # ── Estilos / paleta de colores ───────────────────────────────────────────
    elif "ACTION:SUGGEST_STYLES" in ai_response:
        data = _extract_action_data(ai_response)
        response.action = "SUGGEST_STYLES"
        response.action_data = {
            "cardBg":           data.get("cardBg", "#ffffff"),
            "colorBoton":       data.get("colorBoton", "#000000"),
            "colorTitulo":      data.get("colorTitulo", "#000000"),
            "colorParrafo":     data.get("colorParrafo", "#333333"),
            "cardBorderColor1": data.get("cardBorderColor1", "#cccccc"),
            "cardBorderColor2": data.get("cardBorderColor2", "#cccccc"),
            "cardBorderWidth":  data.get("cardBorderWidth", "1"),
            "cardRadius":       data.get("cardRadius", "8"),
        }

    # ── Componentes visuales (banner, header, footer) ─────────────────────────
    elif "ACTION:SUGGEST_COMPONENTS" in ai_response:
        data = _extract_action_data(ai_response)
        response.action = "SUGGEST_COMPONENTS"
        response.action_data = {
            "banner": {
                "title": data.get("bannerTitle", ""),
                "font":  data.get("bannerFont", "Bebas Neue"),
                "color": data.get("bannerColor", "#ffffff"),
                "bg":    data.get("bannerBg", "#000000"),
            },
            "header": {
                "logo":  data.get("headerLogo", ""),
                "font":  data.get("headerFont", "Inter"),
                "color": data.get("headerColor", "#ffffff"),
                "bg":    data.get("headerBg", "#000000"),
            },
            "footer": {
                "text":  data.get("footerText", ""),
                "font":  data.get("footerFont", "Montserrat"),
                "color": data.get("footerColor", "#888888"),
                "bg":    data.get("footerBg", "#111111"),
            },
        }

    # ── Layout ────────────────────────────────────────────────────────────────
    elif "ACTION:SUGGEST_LAYOUT" in ai_response:
        data = _extract_action_data(ai_response)
        response.action = "SUGGEST_LAYOUT"
        response.action_data = {
            "id":          data.get("layoutId", "clasico"),
            "title":       data.get("layoutTitle", ""),
            "description": data.get("layoutDescription", ""),
        }

    # ── Información legal ─────────────────────────────────────────────────────
    elif "ACTION:SUGGEST_LEGAL" in ai_response:
        data = _extract_action_data(ai_response)
        response.action = "SUGGEST_LEGAL"
        response.action_data = {
            "legalName": data.get("legalName", ""),
            "idNumber":  data.get("idNumber", ""),
        }

    # ── Pago y envío ──────────────────────────────────────────────────────────
    elif "ACTION:SUGGEST_PAYMENT" in ai_response:
        data = _extract_action_data(ai_response)
        response.action = "SUGGEST_PAYMENT"
        response.action_data = {
            "paymentMethod": data.get("paymentMethod", "mercadopago"),
            "shipping":      data.get("shipping", "ambos"),
        }

    return response
