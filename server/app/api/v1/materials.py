from pathlib import Path

from fastapi import APIRouter, HTTPException
from fastapi.responses import FileResponse

from app.services.container import service_container


router = APIRouter()


@router.get("/{material_id}/file")
def get_material_file(material_id: str) -> FileResponse:
    material = service_container.material_service.get_material(material_id)
    if material is None:
        raise HTTPException(status_code=404, detail="MATERIAL_NOT_FOUND")
    path = Path(material.local_path)
    if not path.exists() or not path.is_file():
        raise HTTPException(status_code=404, detail="MATERIAL_FILE_NOT_FOUND")
    return FileResponse(path=path, media_type=material.mime_type, filename=path.name)
