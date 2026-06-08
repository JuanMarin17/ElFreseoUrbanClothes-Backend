import httpx
from config.settings import LOYALTY_SERVICE_URL


async def get_user_account(user_id: str, store_id: str, jwt_token: str, role: str) -> dict:
    """GET /api/v1/loyalty/users/{userId} — cuenta de puntos de un usuario específico (ADMIN/OWNER)."""
    try:
        async with httpx.AsyncClient(timeout=8) as client:
            r = await client.get(
                f"{LOYALTY_SERVICE_URL}/api/v1/loyalty/users/{user_id}",
                headers={
                    "X-Store-Id": store_id,
                    "X-User-Id": user_id,
                    "X-User-Role": role,
                    "Authorization": f"Bearer {jwt_token}",
                }
            )
            if r.status_code == 200:
                return r.json()
    except Exception as e:
        print(f"[loyalty_client] get_user_account error: {e}")
    return {}


async def get_my_account(store_id: str, admin_id: str, jwt_token: str) -> dict:
    """GET /api/v1/loyalty/me — cuenta de puntos del admin autenticado."""
    try:
        async with httpx.AsyncClient(timeout=8) as client:
            r = await client.get(
                f"{LOYALTY_SERVICE_URL}/api/v1/loyalty/me",
                headers={
                    "X-Store-Id": store_id,
                    "X-User-Id": admin_id,
                    "Authorization": f"Bearer {jwt_token}",
                }
            )
            if r.status_code == 200:
                return r.json()
    except Exception as e:
        print(f"[loyalty_client] get_my_account error: {e}")
    return {}


async def get_ledger(store_id: str, admin_id: str, jwt_token: str) -> list:
    """GET /api/v1/loyalty/me/ledger — historial de movimientos de puntos."""
    try:
        async with httpx.AsyncClient(timeout=8) as client:
            r = await client.get(
                f"{LOYALTY_SERVICE_URL}/api/v1/loyalty/me/ledger",
                headers={
                    "X-Store-Id": store_id,
                    "X-User-Id": admin_id,
                    "Authorization": f"Bearer {jwt_token}",
                }
            )
            if r.status_code == 200:
                data = r.json()
                return data if isinstance(data, list) else data.get("data", [])
    except Exception as e:
        print(f"[loyalty_client] get_ledger error: {e}")
    return []
