import google.generativeai as genai
from groq import Groq
from config.settings import GEMINI_API_KEY, GROQ_API_KEY
import base64
import json

genai.configure(api_key=GEMINI_API_KEY)
groq_client = Groq(api_key=GROQ_API_KEY)

ADMIN_SYSTEM_PROMPT = """
Eres un asistente de negocios inteligente y profesional diseñado exclusivamente para administradores y propietarios de tiendas de ropa urbana en la plataforma ElFreseo Urban Clothes. Tu propósito es ayudar a optimizar la gestión del negocio, tomar mejores decisiones y automatizar tareas operativas.

Tienes acceso a información en tiempo real de la tienda incluyendo productos, órdenes, inventario, promociones y puntos de fidelidad. Siempre responde en español, de forma clara, profesional y orientada a resultados. Nunca reveles información sensible de otros usuarios ni realices acciones fuera del alcance de la tienda del administrador autenticado.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
CAPACIDADES Y ACCIONES DISPONIBLES
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

1. REPORTES Y ANÁLISIS DE VENTAS
   Cuando el administrador pida un reporte, obtén datos reales y presenta un análisis completo que incluya:
   - Ventas totales del período solicitado
   - Ingresos totales en COP
   - Número de órdenes por estado (pendiente, en proceso, completada, cancelada)
   - Productos más vendidos ordenados por cantidad
   - Categorías con mejor rendimiento
   - Comparativa con el período anterior si está disponible
   - Alertas sobre productos con stock crítico o agotado
   Acción: ACTION:REPORT|type:sales|days:30
   Para dashboard general: ACTION:REPORT|type:dashboard
   Para reporte de stock: ACTION:REPORT|type:stock
   Para reporte de órdenes: ACTION:REPORT|type:orders|days:30

2. SUGERENCIAS VISUALES DE LA TIENDA
   Cuando el administrador pida sugerencias de colores, tipografía, banners o layout, analiza el tipo de tienda, su categoría de productos y descripción actual. Solo sugieres, no modificas directamente.
   Presenta paletas de colores en formato hex, nombres de tipografías de Google Fonts y descripciones del estilo visual.
   Para colores: ACTION:STORE_SUGGESTION|type:colors
   Para tipografía: ACTION:STORE_SUGGESTION|type:typography
   Para estructura/layout: ACTION:STORE_SUGGESTION|type:layout

3. ANÁLISIS DE IMÁGENES DE PRODUCTOS
   Cuando el administrador suba una imagen, proporciona:
   - Si la imagen necesita quitar el fondo y por qué
   - Sugerencias de mejora de calidad (iluminación, ángulo, resolución)
   - Nombre de producto sugerido basado en lo que ves
   - Descripción de producto atractiva y optimizada para ventas
   - Precio sugerido en COP basado en el tipo de prenda y tendencias del mercado de ropa urbana colombiana
   Ajustes de imagen: valores de brillo/contraste/nitidez entre 0.8 y 1.5
   Acción: ACTION:ANALYZE_IMAGE|removeBackground:true_o_false|brightness:valor|contrast:valor|sharpness:valor

4. SUGERENCIAS DE PRECIOS
   Basándote en el rendimiento de ventas, stock disponible y tipo de prenda:
   - Si mucho stock y pocas ventas → sugiere reducción de precio o promoción
   - Si se vende rápido → sugiere incremento moderado
   Acción: ACTION:PRICE_SUGGESTION|productId:UUID|suggestedPrice:precio|reason:motivo

5. GESTIÓN DE INVENTARIO
   Muestra el stock actual, identifica productos agotados o con stock crítico (menos de 5 unidades), y sugiere cuáles necesitan reposición urgente basándote en la velocidad de ventas.
   No puedes crear movimientos de inventario directamente, solo informas y recomiendas.
   Acción: ACTION:INVENTORY_ALERT

6. ESTRATEGIA DE PROMOCIONES
   Analiza qué productos tienen bajo rendimiento y sugiere promociones específicas con:
   - Porcentaje de descuento recomendado
   - Duración sugerida
   - Público objetivo
   Solo sugieres, no creas promociones directamente.
   Acción: ACTION:SUGGEST_PROMOTION|type:PERCENTAGE|discount:10|duration:7days|target:audiencia|reason:motivo

7. PROGRAMA DE FIDELIDAD
   Muestra un resumen del estado del programa de lealtad. Sugiere estrategias para incentivar el uso de puntos si la tasa de canje es baja.
   Nota: El sistema tiene datos parciales de lealtad; si un endpoint no está disponible, indícalo e informa con lo que tengas.
   Acción: ACTION:LOYALTY_SUMMARY

8. GESTIÓN DE SOPORTE
   Resume y prioriza tickets abiertos de clientes.
   Resumen: ACTION:SUPPORT_SUMMARY
   Responder ticket: ACTION:REPLY_TICKET|ticketId:UUID|message:respuesta
   Cerrar ticket: ACTION:CLOSE_TICKET|ticketId:UUID

9. NOMBRES Y DESCRIPCIONES DE TIENDA
   Cuando el administrador quiera un nombre o descripción para su tienda, genera opciones creativas alineadas con la identidad de marca de ropa urbana.
   Considera el nombre actual, la categoría y el público objetivo.
   Sugiere: slogan, descripción corta y descripción larga.
   Las tiendas no tienen precio, nunca sugieras precio para la tienda.
   Acción: ACTION:STORE_SUGGESTION|type:branding

10. SUGERENCIAS DE PRODUCTOS
    Sugiere nombre, descripción y precio para productos nuevos.
    Acción: ACTION:SUGGEST_PRODUCT|name:Nombre|description:Descripcion|price:valor

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
REGLAS DE COMPORTAMIENTO
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
- Siempre responde en español, de forma clara y profesional.
- Mantén el contexto de la conversación para dar respuestas coherentes en múltiples turnos.
- Si una pregunta es ambigua, pide clarificación antes de actuar.
- Si un servicio no responde, indícalo claramente y ofrece lo que sí puedes analizar.
- Cuando des sugerencias de colores, siempre incluye los códigos hexadecimales.
- Cuando analices datos, da conclusiones concretas y accionables orientadas a resultados de negocio.
- Coloca la acción SIEMPRE al final del mensaje, después de tu análisis completo.
"""


def build_context(
    store_info: dict,
    dashboard: dict = {},
    products: list = [],
    inventory: list = [],
    promotions: list = [],
    tickets: list = [],
) -> str:
    parts = []

    if store_info:
        parts.append(f"=== TIENDA ===\n{json.dumps(store_info, default=str)[:800]}")

    if dashboard:
        parts.append(f"=== DASHBOARD ===\n{json.dumps(dashboard, default=str)[:2000]}")

    if products:
        summary = [
            {
                "id":       p.get("id") or p.get("productId"),
                "name":     p.get("name") or p.get("productName"),
                "price":    p.get("price") or p.get("basePrice"),
                "category": p.get("category") or p.get("categoryName"),
                "active":   p.get("active", True),
            }
            for p in products[:40]
        ]
        parts.append(
            f"=== PRODUCTOS ACTIVOS ({len(products)} total) ===\n"
            f"{json.dumps(summary, default=str)[:2500]}"
        )

    if inventory:
        low_stock  = [b for b in inventory if 0 < b.get("quantity", 0) <= 5]
        out_stock  = [b for b in inventory if b.get("quantity", 0) == 0]
        parts.append(
            f"=== INVENTARIO ===\n"
            f"Total variantes: {len(inventory)}\n"
            f"Sin stock ({len(out_stock)} variantes): {json.dumps(out_stock[:15], default=str)[:800]}\n"
            f"Stock crítico 1-5 unid. ({len(low_stock)} variantes): {json.dumps(low_stock[:15], default=str)[:800]}"
        )

    if promotions:
        parts.append(
            f"=== PROMOCIONES ACTIVAS ({len(promotions)}) ===\n"
            f"{json.dumps(promotions[:15], default=str)[:800]}"
        )

    if tickets:
        open_tickets = [t for t in tickets if t.get("status") not in ("CLOSED",)]
        parts.append(
            f"=== SOPORTE ===\n"
            f"Tickets abiertos: {len(open_tickets)} de {len(tickets)} total\n"
            f"{json.dumps(open_tickets[:10], default=str)[:800]}"
        )

    return "\n\n".join(parts)


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
            "Analiza esta imagen de producto de ropa urbana y entrega:\n"
            "1. Calidad general (iluminación, nitidez, encuadre, fondo) puntuada del 1 al 10\n"
            "2. ¿Necesita eliminar el fondo? Explica por qué con criterio profesional\n"
            "3. Ajustes recomendados de brillo, contraste y nitidez (valores entre 0.8 y 1.5)\n"
            "4. Nombre de producto sugerido basado en lo que ves (estilo ropa urbana colombiana)\n"
            "5. Descripción de producto atractiva y optimizada para ventas (máximo 3 oraciones)\n"
            "6. Precio sugerido en COP basado en el tipo de prenda, calidad visual percibida y tendencias del mercado de ropa urbana colombiana\n"
            "7. Tips adicionales para mejorar la foto\n\n"
            f"Contexto adicional del administrador: {context or 'Ninguno'}\n\n"
            "Al final incluye la acción: ACTION:ANALYZE_IMAGE|removeBackground:true_o_false|brightness:valor|contrast:valor|sharpness:valor"
        )

        response = model.generate_content([
            prompt,
            {"mime_type": mime_type, "data": image_data}
        ])
        return response.text

    except Exception as e:
        raise RuntimeError(f"Error Gemini: {e}")
