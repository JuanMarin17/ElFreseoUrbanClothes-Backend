import httpx
from config.settings import PRODUCT_SERVICE_URL


async def get_active_products(store_id: str, jwt_token: str) -> list:
    """GET /api/v1/products/all/active"""
    try:
        async with httpx.AsyncClient(timeout=8) as client:
            r = await client.get(
                f"{PRODUCT_SERVICE_URL}/api/v1/products/all/active",
                headers={"X-Store-Id": store_id, "Authorization": f"Bearer {jwt_token}"}
            )
            if r.status_code == 200:
                data = r.json()
                return data if isinstance(data, list) else data.get("data", [])
    except Exception as e:
        print(f"[product_client] error: {e}")
    return []
