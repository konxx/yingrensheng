from sqlalchemy import func, select

from app.core.time import now_iso
from app.db.models import MaterialModel, ProjectModel, UploadModel
from app.db.session import SessionLocal
from app.schemas.material import MaterialResponse


class MaterialRepository:
    def create(
        self,
        material_id: str,
        project_id: str,
        title: str,
        material_type: str,
        duration_label: str,
        insight: str,
        local_path: str,
        file_name: str,
        mime_type: str,
        size_bytes: int,
        sha256: str,
        upload_id: str,
    ) -> MaterialResponse:
        with SessionLocal() as session:
            now = now_iso()
            model = MaterialModel(
                material_id=material_id,
                project_id=project_id,
                title=title,
                material_type=material_type,
                duration_label=duration_label,
                insight=insight,
                local_path=local_path,
                file_name=file_name,
                mime_type=mime_type,
                size_bytes=size_bytes,
                sha256=sha256,
                status="LOCAL_SAVED",
                created_at=now,
                updated_at=now,
            )
            session.add(model)

            upload = session.execute(
                select(UploadModel).where(UploadModel.upload_id == upload_id),
            ).scalar_one_or_none()
            if upload is not None:
                upload.material_id = material_id
                upload.status = "LOCAL_SAVED"
                upload.local_path = local_path

            project = session.execute(
                select(ProjectModel).where(ProjectModel.project_id == project_id),
            ).scalar_one_or_none()
            if project is not None:
                material_count = session.execute(
                    select(func.count(MaterialModel.id)).where(MaterialModel.project_id == project_id),
                ).scalar_one()
                project.material_count = int(material_count) + 1
                project.updated_at = now

            session.commit()
            session.refresh(model)
            return self._to_schema(model)

    def list_by_project(self, project_id: str) -> list[MaterialResponse]:
        with SessionLocal() as session:
            models = session.execute(
                select(MaterialModel)
                .where(MaterialModel.project_id == project_id)
                .order_by(MaterialModel.id.desc()),
            ).scalars().all()
            return [self._to_schema(item) for item in models]

    def get_by_material_id(self, material_id: str) -> MaterialResponse | None:
        with SessionLocal() as session:
            model = session.execute(
                select(MaterialModel).where(MaterialModel.material_id == material_id),
            ).scalar_one_or_none()
            return self._to_schema(model) if model else None

    @staticmethod
    def _to_schema(model: MaterialModel) -> MaterialResponse:
        return MaterialResponse(
            materialId=model.material_id,
            projectId=model.project_id,
            title=model.title,
            materialType=model.material_type,
            durationLabel=model.duration_label,
            insight=model.insight,
            localPath=model.local_path,
            previewUrl=f"/api/v1/materials/{model.material_id}/file",
            mimeType=model.mime_type,
            sizeBytes=model.size_bytes,
            status=model.status,
            createdAt=model.created_at,
            updatedAt=model.updated_at,
        )
