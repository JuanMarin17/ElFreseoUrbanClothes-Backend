import httpx
from config.settings import PREFERENCES_SERVICE_URL

async def get_user_behaviors(user_id: str) -> list:
    try:
        async with httpx.AsyncClient() as client:
            response = await client.get(
                f"{PREFERENCES_SERVICE_URL}/api/v1/preferences/behavior",
                headers={"X-User-Id": user_id}
            )
            if response.status_code == 200:
                return response.json()
    except Exception as e:
        print(f"Error obteniendo comportamientos: {e}")
    return []

async def get_user_preferences(user_id: str) -> list:
    try:
        async with httpx.AsyncClient() as client:
            response = await client.get(
                f"{PREFERENCES_SERVICE_URL}/api/v1/preferences",
                headers={"X-User-Id": user_id}
            )
            if response.status_code == 200:
                return response.json()
    except Exception as e:
        print(f"Error obteniendo preferencias: {e}")
    return []