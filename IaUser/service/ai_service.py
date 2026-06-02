import google.generativeai as genai
from groq import Groq
from config.settings import GEMINI_API_KEY, GROQ_API_KEY
import base64
import langdetect

genai.configure(api_key=GEMINI_API_KEY)
groq_client = Groq(api_key=GROQ_API_KEY)

# Mensajes del sistema por idioma
_SYSTEM_TEMPLATES = {
    "es": "Eres un asistente de compras inteligente para {name}, una tienda de {category}.{desc}",
    "en": "You are an intelligent shopping assistant for {name}, a {category} store.{desc}",
    "fr": "Tu es un assistant d'achat intelligent pour {name}, une boutique de {category}.{desc}",
    "pt": "Você é um assistente de compras inteligente para {name}, uma loja de {category}.{desc}",
    "de": "Du bist ein intelligenter Einkaufsassistent für {name}, einen {category}-Shop.{desc}",
}

_CAPABILITIES = {
    "es": """
Puedes ayudar al usuario con:
- Responder preguntas sobre productos usando el contexto proporcionado
- Buscar productos por descripción natural (color, talla, precio, categoría)
- Agregar productos al carrito → ACTION:ADD_TO_CART|variantId:UUID
- Comparar productos → ACTION:COMPARE|productIds:id1,id2
- Resumen de orden antes de pagar → ACTION:ORDER_SUMMARY
- Notificar stock → ACTION:STOCK_NOTIFY|variantId:UUID
- Crear ticket de soporte → ACTION:SUPPORT_TICKET|subject:asunto
- Recomendar productos personalizados → ACTION:GET_RECOMMENDATIONS|query:texto
- Validar cupón → ACTION:VALIDATE_COUPON|code:CODIGO
- Crear reseña de producto → ACTION:CREATE_REVIEW|productId:UUID|rating:5|comment:texto
Sé amigable, conciso y útil. Responde SIEMPRE en el mismo idioma que el usuario.
Cuando el usuario describa características de un producto (color, talla, precio) busca entre los disponibles y sugiere los más relevantes.
""",
    "en": """
You can help the user with:
- Answer questions about products using the provided context
- Search products by natural description (color, size, price, category)
- Add to cart → ACTION:ADD_TO_CART|variantId:UUID
- Compare products → ACTION:COMPARE|productIds:id1,id2
- Order summary → ACTION:ORDER_SUMMARY
- Stock notification → ACTION:STOCK_NOTIFY|variantId:UUID
- Support ticket → ACTION:SUPPORT_TICKET|subject:topic
- Personalized recommendations → ACTION:GET_RECOMMENDATIONS|query:text
- Validate coupon → ACTION:VALIDATE_COUPON|code:CODE
- Create product review → ACTION:CREATE_REVIEW|productId:UUID|rating:5|comment:text
Always respond in the same language as the user.
"""
}


def _detect_language(text: str) -> str:
    try:
        lang = langdetect.detect(text)
        return lang if lang in _SYSTEM_TEMPLATES else "es"
    except Exception:
        return "es"


def build_system_prompt(store_info: dict, language: str = "es") -> str:
    name        = store_info.get("name", "la tienda")
    category    = store_info.get("category", "productos variados")
    description = store_info.get("description", "")
    desc_text   = f" {description}" if description else ""

    template = _SYSTEM_TEMPLATES.get(language, _SYSTEM_TEMPLATES["es"])
    capabilities = _CAPABILITIES.get(language, _CAPABILITIES["es"])

    return template.format(name=name, category=category, desc=desc_text) + capabilities


def generate_response(
    messages: list,
    product_context: str = "",
    store_info: dict = {},
    promotions_context: str = ""
) -> str:
    """Chat de texto con Groq — detecta idioma del último mensaje."""
    try:
        # Detectar idioma del último mensaje del usuario
        last_user = next((m["content"] for m in reversed(messages) if m["role"] == "user"), "")
        language = _detect_language(last_user)

        system = build_system_prompt(store_info, language)

        if product_context:
            label = "Productos disponibles:" if language == "es" else "Available products:"
            system += f"\n\n{label}\n{product_context}"

        if promotions_context:
            label = "Promociones activas:" if language == "es" else "Active promotions:"
            system += f"\n\n{label}\n{promotions_context}"

        groq_messages = [{"role": "system", "content": system}]
        for msg in messages:
            groq_messages.append({
                "role": "assistant" if msg["role"] == "assistant" else "user",
                "content": msg["content"]
            })

        response = groq_client.chat.completions.create(
            model="llama-3.3-70b-versatile",
            messages=groq_messages,
            temperature=0.7,
            max_tokens=1024
        )
        return response.choices[0].message.content

    except Exception as e:
        raise RuntimeError(f"Error al llamar a Groq: {e}")


def generate_with_image(
    message: str,
    image_base64: str,
    mime_type: str,
    store_info: dict = {}
) -> str:
    """Gemini Vision para análisis de imágenes."""
    try:
        language = _detect_language(message)
        model = genai.GenerativeModel("gemini-2.0-flash")
        image_data = base64.b64decode(image_base64)
        system = build_system_prompt(store_info, language)

        if language == "es":
            prompt = f"{system}\nEl usuario compartió una imagen. Analízala y recomienda productos similares.\nMensaje: {message}"
        else:
            prompt = f"{system}\nThe user shared an image. Analyze it and recommend similar products.\nMessage: {message}"

        response = model.generate_content([
            prompt,
            {"mime_type": mime_type, "data": image_data}
        ])
        return response.text

    except Exception as e:
        raise RuntimeError(f"Error al llamar a Gemini con imagen: {e}")


def search_products_by_description(query: str, products: list) -> list:
    """
    Búsqueda conversacional: filtra productos por descripción natural.
    Usa Groq para que el modelo elija los más relevantes.
    """
    if not products or not query:
        return []
    try:
        product_list = "\n".join([
            f"- ID:{p.get('productId', p.get('id', ''))} | {p.get('name','')} | "
            f"Precio:{p.get('price','')} | Categoría:{p.get('categories', p.get('category',''))}"
            for p in products[:50]
        ])

        prompt = f"""El usuario busca: "{query}"
Productos disponibles:
{product_list}

Responde SOLO con los IDs más relevantes separados por comas (máximo 5). Ejemplo: id1,id2,id3"""

        response = groq_client.chat.completions.create(
            model="llama-3.3-70b-versatile",
            messages=[{"role": "user", "content": prompt}],
            temperature=0.3,
            max_tokens=100
        )
        ids_text = response.choices[0].message.content.strip()
        ids = [i.strip() for i in ids_text.split(",") if i.strip()]

        return [p for p in products if str(p.get("productId", p.get("id", ""))) in ids]

    except Exception as e:
        print(f"[search] Error: {e}")
        return []
