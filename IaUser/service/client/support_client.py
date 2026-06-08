import httpx
from config.settings import SUPPORT_SERVICE_URL

async def create_ticket(user_id: str, subject: str):
    try:
        async with httpx.AsyncClient() as client:
            await client.post(
                f"{SUPPORT_SERVICE_URL}/api/v1/support/tickets",
                headers={"X-User-Id": user_id},
                json={"subject": subject}
            )
    except Exception as e:
        print(f"Error creando ticket: {e}")
        raise