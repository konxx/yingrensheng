from pathlib import Path

from fastapi import APIRouter
from fastapi.responses import FileResponse
from fastapi.staticfiles import StaticFiles


WEB_DIR = Path(__file__).resolve().parents[1] / "web"

router = APIRouter()
assets_app = StaticFiles(directory=str(WEB_DIR))


@router.get("/web", include_in_schema=False)
@router.get("/web/", include_in_schema=False)
@router.get("/web/{path:path}", include_in_schema=False)
def web_app(path: str = "") -> FileResponse:
    _ = path
    return FileResponse(WEB_DIR / "index.html")
