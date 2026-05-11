from sqlalchemy import select

from app.core.time import now_iso
from app.db.models import WorkModel
from app.db.session import SessionLocal
from app.schemas.work import WorkResponse


class WorkRepository:
    def list(self) -> list[WorkResponse]:
        with SessionLocal() as session:
            stmt = select(WorkModel).order_by(WorkModel.id.desc())
            models = session.execute(stmt).scalars().all()
            return [
                WorkResponse(
                    workId=model.work_id,
                    projectId=model.project_id,
                    title=model.title,
                    sceneLabel=model.scene_label,
                    durationLabel=model.duration_label,
                    statusLabel=model.status_label,
                    updatedAt=model.updated_at,
                    coverUrl=model.cover_url,
                    videoUrl=model.video_url,
                )
                for model in models
            ]

    def create_or_update(
        self,
        project_id: str,
        title: str,
        scene_label: str,
        duration_label: str,
        status_label: str,
        cover_url: str,
        video_url: str,
    ) -> WorkResponse:
        with SessionLocal() as session:
            model = session.execute(
                select(WorkModel).where(WorkModel.project_id == project_id),
            ).scalar_one_or_none()
            if model is None:
                model = WorkModel(
                    work_id=f"work_{session.query(WorkModel).count() + 1:03d}",
                    project_id=project_id,
                    title=title,
                    scene_label=scene_label,
                    duration_label=duration_label,
                    status_label=status_label,
                    updated_at=now_iso(),
                    cover_url=cover_url,
                    video_url=video_url,
                )
                session.add(model)
            else:
                model.title = title
                model.scene_label = scene_label
                model.duration_label = duration_label
                model.status_label = status_label
                model.cover_url = cover_url
                model.video_url = video_url
                model.updated_at = now_iso()
            session.commit()
            session.refresh(model)
            return WorkResponse(
                workId=model.work_id,
                projectId=model.project_id,
                title=model.title,
                sceneLabel=model.scene_label,
                durationLabel=model.duration_label,
                statusLabel=model.status_label,
                updatedAt=model.updated_at,
                coverUrl=model.cover_url,
                videoUrl=model.video_url,
            )
