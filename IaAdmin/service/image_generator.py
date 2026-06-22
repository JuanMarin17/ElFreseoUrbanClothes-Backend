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

_FALLBACK_DIMENSIONS = {
    "1:1":  (512, 512),
    "3:4":  (512, 682),
    "4:3":  (682, 512),
    "9:16": (512, 910),
    "16:9": (910, 512),
}


def _pollinations_get(prompt: str, width: int, height: int) -> bytes:
    encoded = urllib.parse.quote(prompt, safe="")
    url = (
        f"https://image.pollinations.ai/prompt/{encoded}"
        f"?width={width}&height={height}&model=flux&nologo=1"
    )
    r = httpx.get(url, timeout=90, follow_redirects=True)
    if r.status_code == 200 and r.content:
        return r.content
    raise RuntimeError(f"Pollinations respondió con status {r.status_code}")


def generate_image(prompt: str, aspect_ratio: str = "1:1") -> bytes:
    """Genera una imagen con Pollinations.ai (FLUX.1) — gratis, sin API key."""
    ratio = aspect_ratio if aspect_ratio in _VALID_RATIOS else "1:1"
    w, h = _DIMENSIONS[ratio]
    fw, fh = _FALLBACK_DIMENSIONS[ratio]

    # Intento 1: tamaño completo
    try:
        return _pollinations_get(prompt, w, h)
    except Exception as e1:
        print(f"[image_generator] Pollinations {w}x{h} falló: {e1}, reintentando con tamaño reducido...")

    # Intento 2: tamaño reducido
    try:
        return _pollinations_get(prompt, fw, fh)
    except Exception as e2:
        print(f"[image_generator] Pollinations {fw}x{fh} falló: {e2}")

    raise RuntimeError("No se pudo generar la imagen. Intenta de nuevo en unos segundos.")
