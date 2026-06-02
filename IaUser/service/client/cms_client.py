import httpx
from config.settings import CMS_SERVICE_URL


async def get_recommendations(user_id: str, store_id: str, query: str = "") -> dict:
    """
    Llama al CMS para obtener recomendaciones personalizadas.
    POST /api/v1/cms/generate
    Retorna: { content, recommendations: [{productId, name, price, imageUrl, ...}] }
    """
    try:
        async with httpx.AsyncClient(timeout=10) as client:
            r = await client.post(
                f"{CMS_SERVICE_URL}/api/v1/cms/generate",
                json={"query": query},
                headers={"X-User-Id": user_id, "X-Store-Id": store_id}
            )
            if r.status_code in (200, 201):
                return r.json()
    except Exception as e:
        print(f"[cms_client] Error obteniendo recomendaciones: {e}")
    return {}
