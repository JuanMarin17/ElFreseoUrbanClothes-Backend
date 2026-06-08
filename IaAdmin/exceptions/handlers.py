from fastapi import Request
from fastapi.responses import JSONResponse
from datetime import datetime

async def general_exception_handler(request: Request, exc: Exception):
    return JSONResponse(status_code=500, content={
        "timestamp": datetime.now().isoformat(),
        "status": 500,
        "message": f"Error interno: {str(exc)}"
    })

async def value_error_handler(request: Request, exc: ValueError):
    return JSONResponse(status_code=400, content={
        "timestamp": datetime.now().isoformat(),
        "status": 400,
        "message": str(exc)
    })
