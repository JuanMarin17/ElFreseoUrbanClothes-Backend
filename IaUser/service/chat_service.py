from service.ai_service import generate_response, generate_with_image, search_products_by_description
from service.session_cleaner import trim_session_messages
from sqlalchemy.orm import Session
from models.database import ChatSession, ChatMessage, StockNotification
from service.client import (
    cart_client, support_client, product_client,
    store_client, promotions_client, cms_client, reviews_client
)
from schemas.chat_schemas import ChatRequest, ChatResponse
import uuid
import re
from uuid import UUID

_UUID_RE = re.compile(r'[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}')


def _sanitize_uuid(value: str) -> str:
    match = _UUID_RE.search(value)
    return match.group(0) if match else value


def _build_product_context(products: list) -> str:
    if not products:
        return ""
    lines = []
    for p in products[:40]:
        variants = p.get("variants", [])
        # Precio desde la primera variante (el producto no tiene precio propio)
        first_price = next((v.get("price") for v in variants if v.get("price") is not None), None)
        price_label = f"${first_price}" if first_price is not None else "sin precio"

        stock_info = []
        for v in variants:
            qty   = v.get("stock", 0)
            color = v.get("color", "")
            size  = v.get("size", "")
            label = f"{color}/{size}".strip("/") if (color or size) else "única"
            stock_info.append(
                f"{label}: {'disponible' if qty > 0 else 'sin stock'} (variantId:{v.get('variantId','')})"
            )
        lines.append(
            f"- {p.get('name','')} | productId:{p.get('productId','')} | Precio: {price_label} | Variantes: {', '.join(stock_info)}"
        )
    return "\n".join(lines)


def _build_promotions_context(promotions: list) -> str:
    if not promotions:
        return ""
    lines = []
    for promo in promotions[:10]:
        name      = promo.get("name", "")
        discount  = promo.get("discountValue", promo.get("discount", ""))
        promo_type = promo.get("type", "")
        lines.append(f"- {name} | Descuento: {discount} | Tipo: {promo_type}")
    return "\n".join(lines)


async def process_chat(
    dto: ChatRequest,
    user_id: str,
    store_id: str,
    db: Session
) -> ChatResponse:

    # Sesión
    if dto.session_id:
        session = db.query(ChatSession).filter(
            ChatSession.session_id == dto.session_id
        ).first()
        if not session:
            raise ValueError("Sesión no encontrada")
    else:
        session = ChatSession(
            user_id=uuid.UUID(user_id),
            store_id=uuid.UUID(store_id)
        )
        db.add(session)
        db.commit()
        db.refresh(session)

    # Guardar mensaje
    db.add(ChatMessage(session_id=session.session_id, role="user", content=dto.message))
    db.commit()

    # Limitar mensajes por sesión
    trim_session_messages(db, session.session_id)

    # Historial
    messages = db.query(ChatMessage).filter(
        ChatMessage.session_id == session.session_id
    ).order_by(ChatMessage.created_at.asc()).all()
    history = [{"role": m.role, "content": m.content} for m in messages]

    # Contexto dinámico
    store_info   = await store_client.get_store_info(store_id)
    products     = await product_client.get_active_products(store_id)
    promotions   = await promotions_client.get_active_promotions(store_id)

    product_context    = _build_product_context(products)
    promotions_context = _build_promotions_context(promotions)

    # Llamada a IA
    if dto.image_base64:
        ai_response = generate_with_image(
            dto.message, dto.image_base64,
            dto.image_mime_type or "image/jpeg",
            store_info=store_info
        )
    else:
        ai_response = generate_response(
            history, product_context,
            store_info=store_info,
            promotions_context=promotions_context
        )

    # Guardar respuesta
    db.add(ChatMessage(session_id=session.session_id, role="assistant", content=ai_response))
    db.commit()

    return await process_action(
        session.session_id, ai_response, user_id, store_id, db, products, dto.message
    )


async def process_action(
    session_id: UUID,
    ai_response: str,
    user_id: str,
    store_id: str,
    db: Session,
    products: list = [],
    user_message: str = ""
) -> ChatResponse:

    response = ChatResponse(session_id=session_id, message=ai_response)

    if "ACTION:ADD_TO_CART" in ai_response:
        data = extract_action_data(ai_response)
        product_id = _sanitize_uuid(data.get("productId", ""))
        print(f"[process_action] ADD_TO_CART | productId={product_id} | storeId={store_id} | userId={user_id}")
        response.action = "ADD_TO_CART"
        response.action_data = data
        try:
            await cart_client.add_to_cart(user_id, store_id, product_id, 1)
            response.message = clean_response(ai_response) or "¡Perfecto! Agregué el producto a tu carrito."
        except Exception as e:
            print(f"[process_action] ADD_TO_CART falló: {e}")
            response.message = f"No pude agregar el producto al carrito. Error: {e}"

    elif "ACTION:STOCK_NOTIFY" in ai_response:
        data = extract_action_data(ai_response)
        variant_id = _sanitize_uuid(data.get("variantId", ""))
        response.action = "STOCK_NOTIFY"
        response.action_data = data
        try:
            notif = StockNotification(
                user_id=uuid.UUID(user_id),
                store_id=uuid.UUID(store_id),
                variant_id=uuid.UUID(variant_id) if variant_id else uuid.uuid4()
            )
            db.add(notif)
            db.commit()
            response.message = "¡Listo! Te avisaré en cuanto ese producto vuelva a tener stock. 🔔"
        except:
            response.message = "No pude registrar la notificación ahora. Por favor intenta más tarde."

    elif "ACTION:SUPPORT_TICKET" in ai_response:
        data = extract_action_data(ai_response)
        response.action = "SUPPORT_TICKET"
        response.action_data = data
        try:
            await support_client.create_ticket(user_id, data.get("subject", "Consulta general"))
            response.message = "Creé un ticket de soporte para ti. Nuestro equipo te contactará pronto."
        except:
            response.message = clean_response(ai_response)

    elif "ACTION:ORDER_SUMMARY" in ai_response:
        response.action = "ORDER_SUMMARY"
        response.message = clean_response(ai_response) or "Aquí tienes el resumen de tu orden."

    elif "ACTION:COMPARE" in ai_response:
        response.action = "COMPARE"
        response.action_data = extract_action_data(ai_response)
        response.message = clean_response(ai_response)

    elif "ACTION:GET_RECOMMENDATIONS" in ai_response:
        data = extract_action_data(ai_response)
        query = data.get("query", user_message)
        response.action = "GET_RECOMMENDATIONS"
        try:
            cms_data = await cms_client.get_recommendations(user_id, store_id, query)
            response.message = clean_response(ai_response) or "Aquí tienes productos recomendados para ti."
            response.product_recommendations = cms_data.get("recommendations", [])
            response.action_data = {"reason": cms_data.get("content", "")}
        except:
            response.message = clean_response(ai_response)

    elif "ACTION:VALIDATE_COUPON" in ai_response:
        data = extract_action_data(ai_response)
        code = data.get("code", "")
        response.action = "VALIDATE_COUPON"
        try:
            result = await promotions_client.validate_coupon(store_id, code)
            if result:
                discount = result.get("discountValue", result.get("discount", ""))
                response.message = f"¡Cupón válido! Obtienes un descuento de {discount}. Se aplicará al momento del pago."
                response.action_data = result
            else:
                response.message = "Ese cupón no es válido o ya expiró."
        except:
            response.message = "No pude validar el cupón ahora. Intenta más tarde."

    elif "ACTION:CREATE_REVIEW" in ai_response:
        data = extract_action_data(ai_response)
        response.action = "CREATE_REVIEW"
        try:
            result = await reviews_client.create_review(
                user_id, store_id,
                data.get("productId", ""),
                int(data.get("rating", 5)),
                data.get("comment", "")
            )
            response.message = "¡Gracias por tu reseña! Tu opinión ayuda a otros compradores. ⭐"
            response.action_data = result
        except:
            response.message = "No pude guardar tu reseña ahora. Por favor intenta más tarde."

    elif "ACTION:SEARCH" in ai_response:
        data = extract_action_data(ai_response)
        query = data.get("query", user_message)
        response.action = "SEARCH"
        found = search_products_by_description(query, products)
        if found:
            response.product_recommendations = found
            response.message = clean_response(ai_response) or f"Encontré {len(found)} productos que coinciden con tu búsqueda."
        else:
            response.message = "No encontré productos que coincidan exactamente. ¿Puedes darme más detalles?"

    else:
        response.message = ai_response.strip()

    return response


def extract_action_data(response: str) -> dict:
    data = {}
    try:
        action_part = response[response.index("ACTION:"):]
        # Solo la primera línea — evita que texto de respuesta posterior contamine los valores
        action_line = action_part.split("\n")[0].strip()
        parts = action_line.split("|")
        for part in parts:
            if ":" in part and not part.startswith("ACTION"):
                k, v = part.split(":", 1)
                data[k.strip()] = v.strip()
    except Exception as e:
        print(f"Error extrayendo datos: {e}")
    return data


def clean_response(response: str) -> str:
    if "ACTION:" in response:
        return response[:response.index("ACTION:")].strip()
    return response.strip()
