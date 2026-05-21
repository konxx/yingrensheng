from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles

from app.api.routes import api_router
from app.api.admin import router as admin_router
from app.api.web import assets_app as web_assets_app
from app.api.web import router as web_router
from app.core.settings import settings
from app.db.bootstrap import initialize_database
from pathlib import Path


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
app.include_router(admin_router)
app.mount("/web/assets", web_assets_app, name="web_assets")
app.include_router(web_router)
Path(settings.storage_root).mkdir(parents=True, exist_ok=True)
app.mount("/storage", StaticFiles(directory=settings.storage_root), name="storage")
