import httpx
from config.settings import ANALYTICS_SERVICE_URL

async def get_sales_report(store_id: str, period: str = "monthly") -> dict:
    try:
        async with httpx.AsyncClient() as client:
            r = await client.get(
                f"{ANALYTICS_SERVICE_URL}/api/v1/analytics/sales",
                headers={"X-Store-Id": store_id},
                params={"period": period}
            )
            if r.status_code == 200:
                return r.json()
    except Exception as e:
        print(f"Error obteniendo analytics: {e}")
    return {}

async def get_top_products(store_id: str, limit: int = 10) -> list:
    try:
        async with httpx.AsyncClient() as client:
            r = await client.get(
                f"{ANALYTICS_SERVICE_URL}/api/v1/analytics/top-products",
                headers={"X-Store-Id": store_id},
                params={"limit": limit}
            )
            if r.status_code == 200:
                return r.json().get("data", [])
    except Exception as e:
        print(f"Error obteniendo top productos: {e}")
    return []
