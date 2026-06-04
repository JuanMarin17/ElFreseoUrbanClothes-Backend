from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from models.database import create_tables
from routes.admin_routes import router
from exceptions.handlers import general_exception_handler, value_error_handler

app = FastAPI(title="IA Admin Agent", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.on_event("startup")
async def startup():
    create_tables()

app.include_router(router)
app.add_exception_handler(Exception, general_exception_handler)
app.add_exception_handler(ValueError, value_error_handler)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8102, reload=True)
