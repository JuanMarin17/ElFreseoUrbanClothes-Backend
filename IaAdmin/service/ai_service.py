import google.generativeai as genai
from groq import Groq
from config.settings import GEMINI_API_KEY, GROQ_API_KEY
import base64
import json

genai.configure(api_key=GEMINI_API_KEY)
groq_client = Groq(api_key=GROQ_API_KEY)

ADMIN_SYSTEM_PROMPT = """
Eres un agente de inteligencia artificial especializado para administradores (ADMIN) y propietarios (OWNER) de tiendas en un ecommerce.

Tienes acceso a datos reales de la tienda y puedes ayudar con:

1. REPORTES Y ANÁLISIS
   - Interpretar dashboard, ventas, órdenes y stock
   - Identificar tendencias, alertas y oportunidades
   - Responde con: ACTION:REPORT_DASHBOARD, ACTION:REPORT_SALES|days:30, ACTION:REPORT_STOCK, ACTION:REPORT_ORDERS|days:30

2. ESTRATEGIA DE PRECIOS
   - Analizar precios actuales y sugerir ajustes con justificación
   - Responde con: ACTION:SUGGEST_PRICE|productId:UUID|suggestedPrice:valor|reason:motivo

3. CONFIGURACIÓN VISUAL DE LA TIENDA
   - Sugerir paletas de colores (hex), tipografías, tamaños coherentes con la marca
   - Responde con: ACTION:SUGGEST_STORE_STYLE|primaryColor:#hex|secondaryColor:#hex|accentColor:#hex|headingFont:nombre|bodyFont:nombre|borderRadius:8

4. ANÁLISIS Y MEJORA DE IMÁGENES DE PRODUCTOS
   - Evaluar calidad, iluminación, encuadre de imágenes
   - Detectar si necesita eliminar fondo
   - Responde con: ACTION:ANALYZE_IMAGE|removeBackground:true|brightness:1.1|contrast:1.05|sharpness:1.2

5. SUGERENCIAS DE PRODUCTOS
   - Sugerir nombre, descripción y precio para productos nuevos o existentes
   - Responde con: ACTION:SUGGEST_PRODUCT|name:Nombre|description:Descripcion|price:valor

6. ALERTAS DE INVENTARIO
   - Analizar balance de inventario y alertar sobre stock crítico
   - Responde con: ACTION:INVENTORY_ALERT

7. GESTIÓN DE SOPORTE
   - Resumir y priorizar tickets abiertos
   - Sugerir respuestas para tickets
   - Responde con: ACTION:SUPPORT_SUMMARY
   - Para responder un ticket: ACTION:REPLY_TICKET|ticketId:UUID|message:respuesta
   - Para cerrar: ACTION:CLOSE_TICKET|ticketId:UUID

8. PROMOCIONES
   - Sugerir estrategias de descuentos basadas en ventas y stock
   - Responde con: ACTION:SUGGEST_PROMOTION|type:PERCENTAGE|discount:10|reason:motivo

Sé analítico, directo y proactivo. Responde siempre en español.
Cuando des sugerencias de colores, siempre incluye los códigos hexadecimales.
Cuando analices datos, da conclusiones concretas y accionables.
"""


def build_context(store_info: dict, dashboard: dict = {}, products: list = []) -> str:
    parts = []
    if store_info:
        name = store_info.get("name", store_info.get("basic", {}).get("name", ""))
        parts.append(f"Tienda: {name}")
    if dashboard:
        parts.append(f"Dashboard: {json.dumps(dashboard, default=str)[:1000]}")
    if products:
        parts.append(f"Productos activos: {len(products)}")
    return "\n".join(parts)


def generate_admin_response(messages: list, context: str = "") -> str:
    """Chat de texto para admins con Groq."""
    try:
        system = ADMIN_SYSTEM_PROMPT
        if context:
            system += f"\n\nContexto actual de la tienda:\n{context}"

        groq_messages = [{"role": "system", "content": system}]
        for msg in messages:
            groq_messages.append({
                "role": "assistant" if msg["role"] == "assistant" else "user",
                "content": msg["content"]
            })

        response = groq_client.chat.completions.create(
            model="llama-3.3-70b-versatile",
            messages=groq_messages,
            temperature=0.6,
            max_tokens=2048
        )
        return response.choices[0].message.content

    except Exception as e:
        raise RuntimeError(f"Error Groq: {e}")


def analyze_product_image(image_base64: str, mime_type: str, context: str = "") -> str:
    """Gemini Vision para analizar imagen de producto."""
    try:
        model = genai.GenerativeModel("gemini-2.0-flash")
        image_data = base64.b64decode(image_base64)

        prompt = (
            f"{ADMIN_SYSTEM_PROMPT}\n\n"
            "Analiza esta imagen de producto:\n"
            "1. Evalúa calidad (iluminación, nitidez, encuadre) del 1 al 10\n"
            "2. ¿Necesita eliminar el fondo? ¿Por qué?\n"
            "3. Ajustes de brillo/contraste/nitidez recomendados (valores entre 0.8 y 1.5)\n"
            "4. Sugiere nombre, descripción y precio estimado del producto\n"
            "5. Tips adicionales para mejorar la foto\n\n"
            f"Contexto adicional: {context or 'Ninguno'}\n\n"
            "Termina con la acción: ACTION:ANALYZE_IMAGE|removeBackground:true_o_false|brightness:valor|contrast:valor|sharpness:valor"
        )

        response = model.generate_content([
            prompt,
            {"mime_type": mime_type, "data": image_data}
        ])
        return response.text

    except Exception as e:
        raise RuntimeError(f"Error Gemini: {e}")
