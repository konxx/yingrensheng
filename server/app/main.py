from fastapi import FastAPI

from app.api.routes import api_router
from app.core.settings import settings
from app.db.bootstrap import initialize_database


app = FastAPI(
    title="YingRenSheng Server",
    version="0.1.0",
    description="Backend API for YingRenSheng Android app.",
)


@app.on_event("startup")
def on_startup() -> None:
    initialize_database()


@app.get("/health")
def health() -> dict[str, str | int]:
    return {
        "status": "ok",
        "db_driver": settings.db_driver,
        "port": settings.app_port,
    }


app.include_router(api_router, prefix="/api/v1")
