import httpx
from config.settings import SUPPORT_SERVICE_URL


async def get_all_tickets(store_id: str, jwt_token: str) -> list:
    """GET /api/v1/support/tickets — todos los tickets (solo OWNER)"""
    try:
        async with httpx.AsyncClient(timeout=8) as client:
            r = await client.get(
                f"{SUPPORT_SERVICE_URL}/api/v1/support/tickets",
                headers={"X-Store-Id": store_id, "Authorization": f"Bearer {jwt_token}"}
            )
            if r.status_code == 200:
                return r.json() if isinstance(r.json(), list) else r.json().get("data", [])
    except Exception as e:
        print(f"[support_client] tickets error: {e}")
    return []


async def reply_ticket(ticket_id: str, message: str, jwt_token: str) -> dict:
    """POST /api/v1/support/tickets/{ticketId}/reply"""
    try:
        async with httpx.AsyncClient(timeout=8) as client:
            r = await client.post(
                f"{SUPPORT_SERVICE_URL}/api/v1/support/tickets/{ticket_id}/reply",
                json={"message": message},
                headers={"Authorization": f"Bearer {jwt_token}"}
            )
            if r.status_code in (200, 201):
                return r.json()
    except Exception as e:
        print(f"[support_client] reply error: {e}")
    return {}


async def close_ticket(ticket_id: str, jwt_token: str) -> dict:
    """PATCH /api/v1/support/tickets/{ticketId}/close"""
    try:
        async with httpx.AsyncClient(timeout=8) as client:
            r = await client.patch(
                f"{SUPPORT_SERVICE_URL}/api/v1/support/tickets/{ticket_id}/close",
                headers={"Authorization": f"Bearer {jwt_token}"}
            )
            if r.status_code == 200:
                return r.json()
    except Exception as e:
        print(f"[support_client] close error: {e}")
    return {}
