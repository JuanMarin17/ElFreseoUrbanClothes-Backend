import httpx
from config.settings import CART_SERVICE_URL

async def add_to_cart(user_id: str, store_id: str, product_id: str, quantity: int = 1):
    url = f"{CART_SERVICE_URL}/api/v1/stores/{store_id}/cart/items"
    payload = {"productId": product_id, "quantity": quantity}
    print(f"[cart_client] POST {url} | body={payload} | X-User-Id={user_id}")
    try:
        async with httpx.AsyncClient(timeout=10.0) as client:
            response = await client.post(
                url,
                headers={"X-User-Id": user_id},
                json=payload
            )
            print(f"[cart_client] respuesta {response.status_code}: {response.text[:300]}")
            if response.status_code not in (200, 201):
                raise RuntimeError(f"Cart respondió {response.status_code}: {response.text}")
    except Exception as e:
        print(f"[cart_client] ERROR: {e}")
        raise