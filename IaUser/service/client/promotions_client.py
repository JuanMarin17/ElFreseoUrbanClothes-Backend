import httpx
from config.settings import PROMOTIONS_SERVICE_URL


async def get_active_promotions(store_id: str) -> list:
    """GET /api/v1/promotions — promociones activas de la tienda"""
    try:
        async with httpx.AsyncClient(timeout=5) as client:
            r = await client.get(
                f"{PROMOTIONS_SERVICE_URL}/api/v1/promotions",
                headers={"X-Store-Id": store_id}
            )
            if r.status_code == 200:
                return r.json() if isinstance(r.json(), list) else r.json().get("data", [])
    except Exception as e:
        print(f"[promotions_client] Error: {e}")
    return []


async def validate_coupon(store_id: str, code: str) -> dict:
    """GET /api/v1/coupons/{code}/validate"""
    try:
        async with httpx.AsyncClient(timeout=5) as client:
            r = await client.get(
                f"{PROMOTIONS_SERVICE_URL}/api/v1/coupons/{code}/validate",
                headers={"X-Store-Id": store_id}
            )
            if r.status_code == 200:
                return r.json()
    except Exception as e:
        print(f"[promotions_client] Error validando cupón: {e}")
    return {}
