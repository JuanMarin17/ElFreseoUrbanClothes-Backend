from fastapi import FastAPI
from models.database import create_tables
from routes.admin_routes import router
from exceptions.handlers import general_exception_handler, value_error_handler
from service.ai_service import groq_client

app = FastAPI(title="IA Admin Agent", version="1.0.0")

# El CORS se maneja centralizado en el Gateway (CorsWebFilter). Agregarlo también
# aquí duplica el header Access-Control-Allow-Origin y el navegador rechaza la
# respuesta con net::ERR_FAILED aunque el status real sea 200.

@app.on_event("startup")
async def startup():
    create_tables()
    # Establece la conexión TCP/TLS con Groq para que el primer request real no lo pague
    try:
        groq_client.chat.completions.create(
            model="llama-3.3-70b-versatile",
            messages=[{"role": "user", "content": "hi"}],
            max_tokens=1
        )
    except Exception:
        pass

app.include_router(router)
app.add_exception_handler(Exception, general_exception_handler)
app.add_exception_handler(ValueError, value_error_handler)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8102, reload=True)
