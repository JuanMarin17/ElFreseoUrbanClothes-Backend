import urllib.parse
import httpx

_VALID_RATIOS = {"1:1", "3:4", "4:3", "9:16", "16:9"}

_DIMENSIONS = {
    "1:1":  (1024, 1024),
    "3:4":  (768,  1024),
    "4:3":  (1024, 768),
    "9:16": (576,  1024),
    "16:9": (1024, 576),
}


def generate_image(prompt: str, aspect_ratio: str = "1:1") -> bytes:
    """Genera una imagen con Pollinations.ai (FLUX.1) — gratis, sin API key."""
    ratio = aspect_ratio if aspect_ratio in _VALID_RATIOS else "1:1"
    width, height = _DIMENSIONS[ratio]

    encoded_prompt = urllib.parse.quote(prompt)
    url = f"https://image.pollinations.ai/prompt/{encoded_prompt}"

    try:
        response = httpx.get(
            url,
            params={
                "width":   width,
                "height":  height,
                "model":   "flux",
                "nologo":  "true",
                "seed":    42,
            },
            timeout=60,
            follow_redirects=True,
        )
        if response.status_code == 200 and response.content:
            return response.content
        raise RuntimeError(f"Pollinations respondió con status {response.status_code}")
    except Exception as e:
        raise RuntimeError(f"Error generando imagen: {e}")
