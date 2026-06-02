import asyncio
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from models.database import create_tables
from routes.chat_routes import router
from exceptions.handlers import general_exception_handler, value_error_handler
from service.session_cleaner import clean_old_sessions, start_cleaner
from models.database import SessionLocal

app = FastAPI(title="IA User Agent", version="2.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.on_event("startup")
async def startup():
    create_tables()
    # Limpieza inicial al arrancar
    db = SessionLocal()
    try:
        clean_old_sessions(db)
    finally:
        db.close()
    # Iniciar loop de limpieza en background
    asyncio.create_task(start_cleaner())

app.include_router(router)
app.add_exception_handler(Exception, general_exception_handler)
app.add_exception_handler(ValueError, value_error_handler)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8099, reload=True)
