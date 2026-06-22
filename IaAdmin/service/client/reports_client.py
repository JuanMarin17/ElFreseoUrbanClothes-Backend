import httpx
from config.settings import REPORTS_SERVICE_URL

_TIMEOUT = 5


async def get_dashboard(store_id: str, jwt_token: str) -> dict:
    """GET /api/v1/reports/dashboard — propaga excepciones para el circuit breaker."""
    async with httpx.AsyncClient(timeout=_TIMEOUT) as client:
        r = await client.get(
            f"{REPORTS_SERVICE_URL}/api/v1/reports/dashboard",
            headers={"X-Store-Id": store_id, "Authorization": f"Bearer {jwt_token}"}
        )
        return r.json() if r.status_code == 200 else {}


async def get_sales(store_id: str, jwt_token: str, days: int = 30) -> dict:
    """GET /api/v1/reports/sales?days=30"""
    try:
        async with httpx.AsyncClient(timeout=_TIMEOUT) as client:
            r = await client.get(
                f"{REPORTS_SERVICE_URL}/api/v1/reports/sales",
                headers={"X-Store-Id": store_id, "Authorization": f"Bearer {jwt_token}"},
                params={"days": days}
            )
            if r.status_code == 200:
                return r.json()
    except Exception as e:
        print(f"[reports_client] sales error: {e}")
    return {}


async def get_stock_report(store_id: str, jwt_token: str) -> dict:
    """GET /api/v1/reports/stock"""
    try:
        async with httpx.AsyncClient(timeout=_TIMEOUT) as client:
            r = await client.get(
                f"{REPORTS_SERVICE_URL}/api/v1/reports/stock",
                headers={"X-Store-Id": store_id, "Authorization": f"Bearer {jwt_token}"}
            )
            if r.status_code == 200:
                return r.json()
    except Exception as e:
        print(f"[reports_client] stock error: {e}")
    return {}


async def get_orders_report(store_id: str, jwt_token: str, days: int = 30) -> dict:
    """GET /api/v1/reports/orders?days=30"""
    try:
        async with httpx.AsyncClient(timeout=_TIMEOUT) as client:
            r = await client.get(
                f"{REPORTS_SERVICE_URL}/api/v1/reports/orders",
                headers={"X-Store-Id": store_id, "Authorization": f"Bearer {jwt_token}"},
                params={"days": days}
            )
            if r.status_code == 200:
                return r.json()
    except Exception as e:
        print(f"[reports_client] orders error: {e}")
    return {}
