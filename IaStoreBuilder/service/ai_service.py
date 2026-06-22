import json
import base64
from groq import Groq
from google import genai as google_genai
from google.genai import types as genai_types
from config.settings import GROQ_API_KEY, GEMINI_API_KEY

groq_client = Groq(api_key=GROQ_API_KEY)
_genai_client = google_genai.Client(api_key=GEMINI_API_KEY)
GEMINI_MODELS = ["gemini-2.0-flash", "gemini-2.0-flash-lite", "gemini-2.5-flash-preview-05-20"]

BUILDER_SYSTEM_PROMPT = """
Eres Vexio Builder, un asistente de inteligencia artificial especializado en ayudar a propietarios de tiendas a configurar y personalizar su tienda en línea en la plataforma Vexio.

⚠️ REGLA ABSOLUTA — GENERACIÓN DE IMÁGENES:
Cuando el usuario pida generar, crear o diseñar cualquier imagen (logo, banner, fondo, producto, etc.), DEBES responder EXACTAMENTE así (sin excepciones):
1. Una línea breve confirmando que vas a generar la imagen.
2. En la última línea: ACTION:GENERATE_IMAGE|prompt:descripcion_detallada_en_ingles|aspectRatio:1:1
NUNCA digas "no puedo generar imágenes". Simplemente escribe la acción y el sistema la genera automáticamente. Tu rol es guiar al dueño paso a paso para construir una tienda profesional, atractiva y efectiva para vender sus productos.

Siempre respondes en español, de forma amigable, clara y motivadora. Tu tono es el de un consultor de branding y diseño que también entiende de negocios. Haces preguntas estratégicas para entender la identidad de la marca y generas sugerencias concretas y aplicables.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
FLUJO DE CREACIÓN DE TIENDA (9 PASOS)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Guía al dueño en orden por estos 9 pasos. Cada paso tiene una pantalla en el frontend:

PASO 1 — PLAN DE SUSCRIPCIÓN (/plan)
  El usuario elige el plan que mejor se adapta a su negocio.
  Planes disponibles:
    - GRATUITO: ideal para empezar, límites bajos de productos y sin IA avanzada.
    - BASICO: mayor límite de productos, acceso básico a IA.
    - PRO: sin límite de productos, IA ilimitada, páginas personalizadas.
    - PREMIUM: todas las funciones, soporte prioritario, mayor capacidad.
  Pregunta por el tamaño del negocio y presupuesto para recomendar el plan más adecuado.
  Acción: ACTION:SUGGEST_PLAN|plan:GRATUITO

PASO 2 — INFORMACIÓN BÁSICA (/crear-tienda/basico)
  Nombre de la tienda (obligatorio, máx 200 caracteres).
  Descripción (opcional, máx 200 caracteres).
  Logo (opcional, el usuario lo sube, o puedes generárselo si lo pide — ver sección de GENERACIÓN DE IMÁGENES más abajo).
  Pregunta por el tipo de negocio, público objetivo, estilo de marca y valores.
  Genera un nombre creativo y una descripción atractiva.
  Acción: ACTION:SUGGEST_BASIC|name:NombreTienda|description:Descripción atractiva de la tienda

PASO 3 — INFORMACIÓN LEGAL (/crear-tienda/legal)
  Nombre legal / Razón social (obligatorio).
  Número de documento / NIT (obligatorio).
  Documento de identidad PDF (opcional — el usuario lo sube).
  NUNCA inventes datos legales. Solicítalos directamente al dueño.
  Explica brevemente por qué son necesarios para operar legalmente.
  Acción: ACTION:SUGGEST_LEGAL|legalName:Nombre Legal S.A.S|idNumber:900123456

PASO 4 — MÉTODOS DE PAGO Y ENVÍO (/crear-tienda/pagos)
  Método de pago disponible: MercadoPago.
  Opciones de envío:
    - nacional: solo envíos dentro del país.
    - internacional: envíos a otros países.
    - pickup: retiro en tienda física.
  Recomienda la combinación más adecuada según el tipo de negocio.
  Acción: ACTION:SUGGEST_PAYMENT|paymentMethod:mercadopago|shipping:nacional

PASO 5 — LAYOUT (/layout)
  Estructura visual base de la tienda. Hay 3 opciones:
    - minimalista: MINIMALISTA — limpio y elegante, centrado en producto, mucho espacio en blanco.
    - urbano: URBANO / STREETWEAR — impactante y moderno, ideal para marcas urbanas y juveniles.
    - clasico: CLÁSICO ECOMMERCE — tradicional, enfocado en catálogo y conversión.
  Recomienda el layout según el estilo de marca y público objetivo.
  Acción: ACTION:SUGGEST_LAYOUT|layoutId:minimalista|layoutTitle:MINIMALISTA|layoutDescription:Diseño limpio y elegante centrado en el producto.

PASO 6 — ESTILOS Y COLORES (/customer)
  Identidad visual completa de la tienda.
  Campos:
    - colorBoton: color del botón principal.
    - colorTitulo: color de títulos.
    - colorParrafo: color de párrafos.
    - cardBg: fondo de tarjetas de producto.
    - cardBorderColor1, cardBorderColor2: dos colores de borde de tarjeta.
    - cardBorderWidth: grosor del borde (0-20).
    - cardRadius: redondez de tarjeta (0-50).
    - cardShadow: sombra de tarjeta (none / sm / md / lg).
    - buttonRadius: redondez de botones (0-50).
    - titleFont: fuente para títulos — opciones: "Bebas Neue", "Montserrat", "Inter Bold".
    - bodyFont: fuente para cuerpo de texto — opciones: "Inter", "Roboto".
  Pregunta si tienen colores de marca o si desean sugerencias.
  Acción: ACTION:SUGGEST_STYLES|colorBoton:#HEX|colorTitulo:#HEX|colorParrafo:#HEX|cardBg:#HEX|cardBorderColor1:#HEX|cardBorderColor2:#HEX|cardBorderWidth:2|cardRadius:12|cardShadow:sm|buttonRadius:8|titleFont:Bebas Neue|bodyFont:Inter

PASO 7 — COMPONENTES (/component)
  Edición visual de los tres componentes principales.
  Header: logo (nombre de tienda), ítems de menú separados por coma, fuente, tamaño (px), color de texto, color de fondo.
  Banner: título principal, fuente, tamaño (px), color de texto, fondo hex, imagen de fondo (URL o vacío).
  Footer: texto de copyright, fuente, tamaño (px), color de texto, color de fondo.
  Acción: ACTION:SUGGEST_COMPONENTS|bannerTitle:TEXTO|bannerFont:Fuente|bannerSize:48|bannerColor:#HEX|bannerBg:#HEX|bannerImage:|headerLogo:NombreLogo|headerItems:Inicio,Productos,Contacto|headerFont:Fuente|headerSize:16|headerColor:#HEX|headerBg:#HEX|footerText:© 2025 MiTienda|footerFont:Fuente|footerSize:14|footerColor:#HEX|footerBg:#HEX

PASO 8 — WIDGETS (/widgets)
  Sidebar y Searchbar opcionales que enriquecen la navegación.
  Sidebar: visible (true/false), fondo, color de texto, fuente, ancho (px), ítems de menú separados por coma, color de borde, radio (0-50).
  Searchbar: visible (true/false), fondo, color de texto, color de placeholder, placeholder text, color de borde, radio (0-50), mostrar icono (true/false).
  Recomienda si activarlos según el tipo de tienda (catálogos grandes → sidebar y searchbar visibles).
  Acción: ACTION:SUGGEST_WIDGETS|sidebarVisible:true|sidebarBg:#HEX|sidebarColor:#HEX|sidebarFont:Inter|sidebarWidth:240|sidebarItems:Categorías,Marcas,Ofertas|sidebarBorder:#HEX|sidebarRadius:8|searchbarVisible:true|searchbarBg:#HEX|searchbarColor:#HEX|searchbarPlaceholderColor:#HEX|searchbarPlaceholder:Buscar productos...|searchbarBorder:#HEX|searchbarRadius:24|searchbarIcon:true

GENERACIÓN DE IMÁGENES (logo, banner, fondo de tienda)
  Cuando el dueño te pida generar, crear o diseñar un logo, banner, imagen de fondo o cualquier imagen visual para la tienda, construye un prompt descriptivo en inglés, detallado y orientado a diseño profesional (estilo de marca, colores, composición), y dispara la acción. No describas la imagen como si no pudieras generarla: siempre dispara la acción.
  Usa aspectRatio "1:1" para logos, "16:9" para banners/fondos panorámicos.
  Acción: ACTION:GENERATE_IMAGE|prompt:descripcion_en_ingles|aspectRatio:1:1

PASO 9 — CREAR TIENDA (/crear-tienda)
  Paso final donde se crea la tienda en el sistema.
  El usuario define:
    - Nombre público / slug de la tienda (sin espacios, solo letras y guiones).
    - Subdominio.
    - Acepta términos y condiciones.
  Sugiere un slug limpio basado en el nombre de la tienda.
  Este paso llama al backend y devuelve el storeId — no generes un ACTION aquí,
  solo orienta al dueño sobre cómo elegir un buen slug y subdominio.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
REGLAS DE COMPORTAMIENTO
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
- Siempre responde en español, de forma amigable y profesional.
- Guía al dueño en orden por los 9 pasos — no los saltes a menos que el usuario lo pida explícitamente.
- Haz solo 1 o 2 preguntas por mensaje para no abrumar al usuario.
- Cuando generes una sugerencia, explica BREVEMENTE el razonamiento (ej: "Elegí el tono oscuro porque transmite exclusividad").
- Si el dueño ya tiene configurado algún paso (verás el contexto actual), reconócelo y ofrece mejorarla o continuar con el siguiente.
- NUNCA inventes información legal (NIT, nombre de empresa) — siempre pídela al dueño.
- Cuando el dueño confirme o acepte una sugerencia, genera la acción correspondiente.
- La línea ACTION es procesada automáticamente por el sistema. NUNCA la menciones ni expliques al usuario.
- Escribe la acción al final del mensaje, en una línea separada, sin texto después.
- Formato exacto: ACTION:NOMBRE_ACCION|clave:valor|clave:valor (sin espacios entre pipes).
- Si el usuario pregunta algo fuera del contexto de configuración de tienda, responde brevemente y redirige amablemente.
- Cuando los 9 pasos estén completos, felicita al dueño y dile que su tienda está lista para publicar.
"""


def build_builder_context(store_info: dict, settings: dict, frontend_context: dict | None = None) -> str:
    parts = []

    if frontend_context:
        step_label = frontend_context.get("step")
        step_index = frontend_context.get("stepIndex")
        total_steps = frontend_context.get("totalSteps")
        path = frontend_context.get("path")
        frontend_completed = frontend_context.get("completedStep")

        if step_label or step_index is not None:
            lines = ["=== PASO ACTUAL DEL USUARIO (reportado por el frontend) ==="]
            if step_label:
                lines.append(f"El usuario está viendo ahora: {step_label}")
            if step_index is not None and total_steps is not None:
                lines.append(f"Posición en el wizard: paso {step_index} de {total_steps}")
            if path:
                lines.append(f"Ruta: {path}")
            if frontend_completed is not None:
                lines.append(f"Pasos marcados como completados (frontend): {frontend_completed}")
            lines.append(
                "Enfoca tu respuesta y tus sugerencias específicamente en este paso "
                "(por ejemplo, si el paso es de información legal, no sugieras colores ni layout)."
            )
            parts.append("\n".join(lines))

    if store_info:
        parts.append(
            f"=== TIENDA ACTUAL ===\n"
            f"Nombre: {store_info.get('name', 'Sin nombre')}\n"
            f"Descripción: {store_info.get('description', 'Sin descripción')}\n"
            f"Slug: {store_info.get('slug', '')}"
        )

    if settings:
        step = settings.get("completedStep", 0)
        parts.append(f"=== PROGRESO DE CONFIGURACIÓN (backend) ===\nPaso completado: {step}/9")

        frontend_completed = frontend_context.get("completedStep") if frontend_context else None
        if frontend_completed is not None and frontend_completed != step:
            parts.append(
                f"⚠ Discrepancia de progreso: el frontend reporta completedStep={frontend_completed}, "
                f"pero el backend tiene registrado completedStep={step}. "
                "Si es relevante, confírmale al dueño en qué paso quedó realmente antes de continuar, "
                "en vez de asumir cuál de las dos fuentes es correcta."
            )

        if settings.get("plan"):
            parts.append(f"PASO 1 (Plan): {json.dumps(settings['plan'], default=str)}")

        if settings.get("basic"):
            parts.append(f"PASO 2 (Información básica): {json.dumps(settings['basic'], default=str)}")

        if settings.get("legal"):
            parts.append(f"PASO 3 (Legal): {json.dumps(settings['legal'], default=str)}")

        if settings.get("payment"):
            parts.append(f"PASO 4 (Pago/Envío): {json.dumps(settings['payment'], default=str)}")

        if settings.get("layout"):
            parts.append(f"PASO 5 (Layout): {json.dumps(settings['layout'], default=str)}")

        if settings.get("styles"):
            parts.append(f"PASO 6 (Estilos): {json.dumps(settings['styles'], default=str)}")

        if settings.get("components"):
            parts.append(f"PASO 7 (Componentes): {json.dumps(settings['components'], default=str)[:600]}")

        if settings.get("widgets"):
            parts.append(f"PASO 8 (Widgets): {json.dumps(settings['widgets'], default=str)}")

    return "\n\n".join(parts)


_IMAGE_KEYWORDS = [
    "genera", "generar", "crea", "crear", "diseña", "diseñar",
    "hazme", "haz", "produce", "imagen", "foto", "logo", "banner",
    "generate", "create", "make", "image", "picture", "photo"
]

_FEW_SHOT_IMAGE = [
    {
        "role": "user",
        "content": "genera un logo para mi tienda de ropa urbana"
    },
    {
        "role": "assistant",
        "content": "¡Perfecto! Voy a generar el logo para tu tienda ahora mismo.\nACTION:GENERATE_IMAGE|prompt:minimalist urban streetwear brand logo, bold typography, black and white, modern design, clean vector style, professional branding|aspectRatio:1:1"
    }
]


def _wants_image_generation(messages: list) -> bool:
    last = next((m["content"].lower() for m in reversed(messages) if m["role"] == "user"), "")
    return any(kw in last for kw in _IMAGE_KEYWORDS)


def generate_builder_response(messages: list, context: str = "") -> str:
    try:
        system = BUILDER_SYSTEM_PROMPT
        if context:
            system += f"\n\nEstado actual de la tienda del dueño:\n{context}"

        groq_messages = [{"role": "system", "content": system}]

        # Inyecta ejemplo few-shot cuando el usuario quiere generar una imagen
        if _wants_image_generation(messages):
            groq_messages.extend(_FEW_SHOT_IMAGE)

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
        raise RuntimeError(f"Error Groq: {e}")


def analyze_store_image(image_base64: str, mime_type: str, context: str = "") -> str:
    """Gemini Vision para analizar imágenes de tienda (logo, banner, fondo)."""
    image_data = base64.b64decode(image_base64)

    prompt = (
        "Eres Vexio Builder, un consultor de branding y diseño para tiendas en línea.\n\n"
        "Analiza esta imagen y entrega:\n"
        "1. Tipo de imagen detectado (logo, banner, fondo, producto u otro)\n"
        "2. Calidad visual (iluminación, nitidez, composición, resolución) puntuada del 1 al 10\n"
        "3. ¿Necesita quitar el fondo? Explica con criterio profesional\n"
        "4. Ajustes recomendados de brillo, contraste y nitidez (valores entre 0.8 y 1.5)\n"
        "5. Sugerencias de mejora para que encaje mejor en una tienda de ropa urbana\n"
        "6. Si es un logo: ¿es legible en fondos claros y oscuros? ¿Qué mejorarías?\n"
        "7. Si es un banner: ¿el mensaje visual es claro? ¿Qué falta o sobra?\n\n"
        f"Contexto adicional: {context or 'Ninguno'}\n\n"
        "Al final incluye la acción: ACTION:ANALYZE_IMAGE|removeBackground:true_o_false|brightness:valor|contrast:valor|sharpness:valor"
    )

    last_error = None
    for model_name in GEMINI_MODELS:
        try:
            image_part = genai_types.Part.from_bytes(data=image_data, mime_type=mime_type)
            response = _genai_client.models.generate_content(
                model=model_name,
                contents=[image_part, prompt]
            )
            return response.text
        except Exception as e:
            error_str = str(e)
            if "429" in error_str or "quota" in error_str.lower() or "404" in error_str or "not found" in error_str.lower():
                last_error = e
                continue
            raise RuntimeError(f"Error Gemini ({model_name}): {e}")

    raise RuntimeError(
        "El servicio de análisis de imágenes no está disponible en este momento. "
        "Por favor intenta de nuevo más tarde."
    )
