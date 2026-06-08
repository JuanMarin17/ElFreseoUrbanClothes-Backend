import httpx
from config.settings import REVIEWS_SERVICE_URL


async def create_review(user_id: str, store_id: str, product_id: str, rating: int, comment: str) -> dict:
    """POST /api/v1/reviews"""
    try:
        async with httpx.AsyncClient(timeout=5) as client:
            r = await client.post(
                f"{REVIEWS_SERVICE_URL}/api/v1/reviews",
                json={"productId": product_id, "rating": rating, "comment": comment},
                headers={"X-User-Id": user_id, "X-Store-Id": store_id}
            )
            if r.status_code in (200, 201):
                return r.json()
    except Exception as e:
        print(f"[reviews_client] Error creando reseña: {e}")
    return {}


async def get_pending_reviews(user_id: str, store_id: str) -> list:
    """GET /api/v1/reviews/pending — productos comprados sin reseña"""
    try:
        async with httpx.AsyncClient(timeout=5) as client:
            r = await client.get(
                f"{REVIEWS_SERVICE_URL}/api/v1/reviews/pending",
                headers={"X-User-Id": user_id, "X-Store-Id": store_id}
            )
            if r.status_code == 200:
                return r.json() if isinstance(r.json(), list) else r.json().get("data", [])
    except Exception as e:
        print(f"[reviews_client] Error obteniendo reseñas pendientes: {e}")
    return []
