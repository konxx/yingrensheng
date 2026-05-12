import hashlib
import os
import uuid
from pathlib import Path

from fastapi import UploadFile

from app.core.settings import settings
from app.core.time import now_iso
from app.repositories.material_repository import MaterialRepository
from app.repositories.upload_repository import UploadRepository
from app.schemas.material import MaterialResponse
from app.schemas.upload import UploadInitiateRequest


class MaterialService:
    def __init__(
        self,
        material_repository: MaterialRepository,
        upload_repository: UploadRepository,
    ) -> None:
        self.material_repository = material_repository
        self.upload_repository = upload_repository
        self.material_root = Path(settings.storage_root).resolve() / "materials"
        self.material_root.mkdir(parents=True, exist_ok=True)

    def save_uploads(
        self,
        project_id: str,
        files: list[UploadFile],
    ) -> list[MaterialResponse]:
        responses: list[MaterialResponse] = []
        for upload_file in files:
            file_bytes = upload_file.file.read()
            sha256 = hashlib.sha256(file_bytes).hexdigest()
            extension = Path(upload_file.filename or "").suffix or self._suffix_from_mime(upload_file.content_type or "")
            material_id = f"material_{uuid.uuid4().hex[:10]}"
            safe_name = f"{material_id}{extension}"
            target_dir = self.material_root / project_id
            target_dir.mkdir(parents=True, exist_ok=True)
            target_path = target_dir / safe_name
            target_path.write_bytes(file_bytes)

            upload_response = self.upload_repository.create(
                UploadInitiateRequest(
                    fileName=upload_file.filename or safe_name,
                    mimeType=upload_file.content_type or "application/octet-stream",
                    sizeBytes=len(file_bytes),
                    sha256=sha256,
                    projectId=project_id,
                ),
            )

            material_type = self._material_type(upload_file.content_type or "")
            material = self.material_repository.create(
                material_id=material_id,
                project_id=project_id,
                title=Path(upload_file.filename or safe_name).stem,
                material_type=material_type,
                duration_label="-",
                insight="已本地保存，可用于后续 AI 生成",
                local_path=str(target_path),
                file_name=upload_file.filename or safe_name,
                mime_type=upload_file.content_type or "application/octet-stream",
                size_bytes=len(file_bytes),
                sha256=sha256,
                upload_id=upload_response.upload_id,
            )
            responses.append(material)
        return responses

    def list_materials(self, project_id: str) -> list[MaterialResponse]:
        return self.material_repository.list_by_project(project_id)

    @staticmethod
    def _material_type(mime_type: str) -> str:
        if mime_type.startswith("image/"):
            return "PHOTO"
        if mime_type.startswith("video/"):
            return "VIDEO"
        return "NOTE"

    @staticmethod
    def _suffix_from_mime(mime_type: str) -> str:
        if mime_type == "image/jpeg":
            return ".jpg"
        if mime_type == "image/png":
            return ".png"
        if mime_type == "video/mp4":
            return ".mp4"
        return ".bin"
