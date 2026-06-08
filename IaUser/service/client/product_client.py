import httpx
from config.settings import PRODUCT_SERVICE_URL

async def get_active_products(store_id: str) -> list:
    try:
        async with httpx.AsyncClient() as client:
            response = await client.get(
                f"{PRODUCT_SERVICE_URL}/api/v1/products/all/active",
                headers={"X-Store-Id": store_id}
            )
            if response.status_code == 200:
                return response.json().get("data", [])
    except Exception as e:
        print(f"Error obteniendo productos: {e}")
    return []