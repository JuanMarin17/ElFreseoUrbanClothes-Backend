from google import genai
from google.genai import types
from config.settings import GEMINI_API_KEY

_VALID_RATIOS = {"1:1", "3:4", "4:3", "9:16", "16:9"}


def generate_image(prompt: str, aspect_ratio: str = "1:1") -> bytes:
    """Genera una imagen con Imagen 4 de Google y devuelve los bytes PNG."""
    if not GEMINI_API_KEY:
        raise RuntimeError("GEMINI_API_KEY no está configurada")

    ratio = aspect_ratio if aspect_ratio in _VALID_RATIOS else "1:1"
    client = genai.Client(api_key=GEMINI_API_KEY)

    # Intento 1: Imagen 4 Fast (generate_images API)
    try:
        response = client.models.generate_images(
            model="imagen-4.0-fast-generate-001",
            prompt=prompt,
            config=types.GenerateImagesConfig(
                number_of_images=1,
                aspect_ratio=ratio,
            ),
        )
        if response.generated_images:
            return response.generated_images[0].image.image_bytes
    except Exception as e1:
        print(f"[image_generator] Imagen 4 falló: {e1}, intentando con Gemini 3.1 Flash Image...")

    # Intento 2: Gemini 3.1 Flash Image (generate_content con modality IMAGE)
    try:
        response = client.models.generate_content(
            model="gemini-3.1-flash-image",
            contents=prompt,
            config=types.GenerateContentConfig(
                response_modalities=["TEXT", "IMAGE"]
            ),
        )
        for part in response.candidates[0].content.parts:
            if part.inline_data is not None:
                return part.inline_data.data
    except Exception as e2:
        print(f"[image_generator] Gemini 3.1 Flash Image falló: {e2}")

    raise RuntimeError("No se pudo generar la imagen. Intenta de nuevo más tarde.")
