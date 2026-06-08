import httpx
from config.settings import INVENTORY_SERVICE_URL


async def get_balance(store_id: str, jwt_token: str) -> list:
    """GET /api/v1/inventory/balance"""
    try:
        async with httpx.AsyncClient(timeout=8) as client:
            r = await client.get(
                f"{INVENTORY_SERVICE_URL}/api/v1/inventory/balance",
                headers={"X-Store-Id": store_id, "Authorization": f"Bearer {jwt_token}"}
            )
            if r.status_code == 200:
                return r.json() if isinstance(r.json(), list) else r.json().get("data", [])
    except Exception as e:
        print(f"[inventory_client] balance error: {e}")
    return []


async def get_movements(store_id: str, jwt_token: str) -> list:
    """GET /api/v1/inventory/movements"""
    try:
        async with httpx.AsyncClient(timeout=8) as client:
            r = await client.get(
                f"{INVENTORY_SERVICE_URL}/api/v1/inventory/movements",
                headers={"X-Store-Id": store_id, "Authorization": f"Bearer {jwt_token}"}
            )
            if r.status_code == 200:
                return r.json() if isinstance(r.json(), list) else r.json().get("data", [])
    except Exception as e:
        print(f"[inventory_client] movements error: {e}")
    return []
