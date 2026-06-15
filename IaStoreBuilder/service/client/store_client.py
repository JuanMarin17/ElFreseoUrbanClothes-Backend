import httpx
from config.settings import STORE_SERVICE_URL


async def get_user_store_role(store_id: str, user_id: str) -> str | None:
    """GET /api/v1/stores/{storeId}/isOwner/{userId} → 'OWNER' | 'ADMIN' | 'STAFF' | None"""
    try:
        async with httpx.AsyncClient(timeout=5) as client:
            r = await client.get(
                f"{STORE_SERVICE_URL}/api/v1/stores/{store_id}/isOwner/{user_id}"
            )
            if r.status_code == 200:
                return r.json()
    except Exception as e:
        print(f"[store_client] role error: {e}")
    return None


async def get_store_info(store_id: str) -> dict:
    """GET /api/v1/stores/{storeId}"""
    try:
        async with httpx.AsyncClient(timeout=5) as client:
            r = await client.get(
                f"{STORE_SERVICE_URL}/api/v1/stores/{store_id}"
            )
            if r.status_code == 200:
                return r.json()
    except Exception as e:
        print(f"[store_client] info error: {e}")
    return {}


async def get_store_settings(store_id: str, jwt_token: str) -> dict:
    """GET /api/v1/stores/settings/getSettings"""
    try:
        async with httpx.AsyncClient(timeout=5) as client:
            r = await client.get(
                f"{STORE_SERVICE_URL}/api/v1/stores/settings/getSettings",
                headers={"X-Store-Id": store_id, "Authorization": f"Bearer {jwt_token}"}
            )
            if r.status_code == 200:
                return r.json()
    except Exception as e:
        print(f"[store_client] settings error: {e}")
    return {}
