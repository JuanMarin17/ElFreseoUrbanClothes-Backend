from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from models.database import create_tables
from routes.builder_routes import router
from exceptions.handlers import general_exception_handler, value_error_handler
from service.ai_service import groq_client

app = FastAPI(title="IA Store Builder", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.on_event("startup")
async def startup():
    create_tables()
    # Pre-calienta la conexión TLS con Groq para que el primer request real no lo pague
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
    uvicorn.run("main:app", host="0.0.0.0", port=8113, reload=True)
