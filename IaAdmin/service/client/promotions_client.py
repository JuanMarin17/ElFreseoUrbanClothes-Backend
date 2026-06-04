import httpx
from config.settings import PROMOTIONS_SERVICE_URL


async def get_active_promotions(store_id: str, jwt_token: str) -> list:
    """GET /api/v1/promotions"""
    try:
        async with httpx.AsyncClient(timeout=5) as client:
            r = await client.get(
                f"{PROMOTIONS_SERVICE_URL}/api/v1/promotions",
                headers={"X-Store-Id": store_id, "Authorization": f"Bearer {jwt_token}"}
            )
            if r.status_code == 200:
                data = r.json()
                return data if isinstance(data, list) else data.get("data", [])
    except Exception as e:
        print(f"[promotions_client] error: {e}")
    return []
