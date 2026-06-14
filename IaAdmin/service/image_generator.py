from google import genai
from google.genai import types
from config.settings import GEMINI_API_KEY

_client = genai.Client(api_key=GEMINI_API_KEY)


def generate_image(prompt: str, aspect_ratio: str = "1:1") -> bytes:
    """Genera una imagen con Gemini 2.0 Flash (gratuito) y devuelve los bytes PNG."""
    try:
        full_prompt = f"{prompt}. Aspect ratio: {aspect_ratio}." if aspect_ratio != "1:1" else prompt
        response = _client.models.generate_content(
            model="gemini-2.0-flash-preview-image-generation",
            contents=full_prompt,
            config=types.GenerateContentConfig(
                response_modalities=["TEXT", "IMAGE"]
            ),
        )
        for part in response.candidates[0].content.parts:
            if part.inline_data is not None:
                return part.inline_data.data
        raise RuntimeError("La IA no generó ninguna imagen")
    except Exception as e:
        raise RuntimeError(f"Error generando imagen: {e}")
