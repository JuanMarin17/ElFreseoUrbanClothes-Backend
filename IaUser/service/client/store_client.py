import httpx
from config.settings import STORE_SERVICE_URL


async def get_store_info(store_id: str) -> dict:
    """
    Consulta GET /api/v1/stores/getByStoreId/{storeId}
    Retorna campos relevantes para el system prompt:
      name, description (de basic) y category si existe.
    En caso de error retorna {} y el prompt usará valores genéricos.
    """
    try:
        async with httpx.AsyncClient(timeout=5) as client:
            r = await client.get(
                f"{STORE_SERVICE_URL}/api/v1/stores/getByStoreId/{store_id}"
            )
            if r.status_code == 200:
                data = r.json()

                # Extraer campos del StoreResponseDTO
                # Intentamos sacar name y description desde basic (settings)
                # o directamente si vienen en el root
                name = (
                    data.get("name")
                    or data.get("basic", {}).get("name")
                    or "la tienda"
                )
                description = (
                    data.get("description")
                    or data.get("basic", {}).get("description")
                    or ""
                )
                category = data.get("category", "")

                return {
                    "name": name,
                    "description": description,
                    "category": category
                }
    except Exception as e:
        print(f"[store_client] Error obteniendo tienda {store_id}: {e}")
    return {}
