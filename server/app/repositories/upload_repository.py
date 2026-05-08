from sqlalchemy import select

from app.db.models import UploadModel
from app.db.session import SessionLocal
from app.schemas.upload import UploadInitiateRequest, UploadInitiateResponse, UploadStatusResponse


class UploadRepository:
    def create(self, payload: UploadInitiateRequest) -> UploadInitiateResponse:
        with SessionLocal() as session:
            next_number = session.query(UploadModel).count() + 1
            upload_id = f"upload_{next_number:03d}"
            total_parts = max(1, (payload.size_bytes + 5242879) // 5242880)
            model = UploadModel(
                upload_id=upload_id,
                project_id=payload.project_id,
                file_name=payload.file_name,
                mime_type=payload.mime_type,
                size_bytes=payload.size_bytes,
                sha256=payload.sha256,
                status="PENDING",
                uploaded_part_count=0,
                total_part_count=total_parts,
                deduplicated=False,
                material_id=None,
            )
            session.add(model)
            session.commit()
            return UploadInitiateResponse(
                uploadId=upload_id,
                storageProvider="S3_COMPATIBLE",
                bucket="yingrensheng-materials",
                objectKey=f"{payload.project_id}/{upload_id}/{payload.file_name}",
                uploadMode="MULTIPART",
                partSizeBytes=5242880,
                alreadyUploaded=False,
                presignedUrls=[],
            )

    def get(self, upload_id: str) -> UploadStatusResponse | None:
        with SessionLocal() as session:
            stmt = select(UploadModel).where(UploadModel.upload_id == upload_id)
            model = session.execute(stmt).scalar_one_or_none()
            if model is None:
                return None
            return UploadStatusResponse(
                uploadId=model.upload_id,
                status=model.status,
                uploadedPartCount=model.uploaded_part_count,
                totalPartCount=model.total_part_count,
                deduplicated=model.deduplicated,
                materialId=model.material_id,
            )

