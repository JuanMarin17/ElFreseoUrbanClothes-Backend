"""
Procesador de imágenes de productos con Pillow.
"""
import base64
import io
from PIL import Image, ImageEnhance


def enhance_image(
    image_base64: str,
    mime_type: str = "image/jpeg",
    remove_background: bool = False,
    brightness: float = 1.0,
    contrast: float = 1.0,
    sharpness: float = 1.2,
) -> tuple:
    image_data = base64.b64decode(image_base64)
    img = Image.open(io.BytesIO(image_data)).convert("RGBA")

    if remove_background:
        try:
            from rembg import remove as rembg_remove
            img = rembg_remove(img)
        except ImportError:
            pass

    working = img.convert("RGB") if not remove_background else img

    if brightness != 1.0:
        working = ImageEnhance.Brightness(working).enhance(brightness)
    if contrast != 1.0:
        working = ImageEnhance.Contrast(working).enhance(contrast)
    if sharpness != 1.0:
        working = ImageEnhance.Sharpness(working).enhance(sharpness)

    # Escalar si es muy pequeña
    w, h = working.size
    if w < 800 or h < 800:
        scale = max(800 / w, 800 / h)
        working = working.resize((int(w * scale), int(h * scale)), Image.LANCZOS)

    output = io.BytesIO()
    fmt = "PNG" if remove_background else "JPEG"
    save_kwargs = {"quality": 95, "optimize": True} if fmt == "JPEG" else {}
    working.save(output, format=fmt, **save_kwargs)
    output.seek(0)

    return base64.b64encode(output.read()).decode("utf-8"), f"image/{fmt.lower()}"
