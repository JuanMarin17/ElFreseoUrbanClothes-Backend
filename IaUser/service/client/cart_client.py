import httpx
from config.settings import CART_SERVICE_URL

async def add_to_cart(user_id: str, store_id: str, variant_id: str, quantity: int = 1):
    try:
        async with httpx.AsyncClient() as client:
            await client.post(
                f"{CART_SERVICE_URL}/api/v1/cart/items",
                headers={"X-User-Id": user_id, "X-Store-Id": store_id},
                json={"variantId": variant_id, "quantity": quantity}
            )
    except Exception as e:
        print(f"Error agregando al carrito: {e}")
        raise