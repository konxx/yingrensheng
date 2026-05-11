from sqlalchemy import delete, select

from app.core.time import now_iso
from app.db.models import PreviewAssetModel, StoryDraftModel, StoryboardSectionModel, TaskModel
from app.db.session import SessionLocal
from app.schemas.creation import PreviewAssetResponse, StoryDraftResponse, StoryboardSectionResponse
from app.schemas.task import TaskStatusResponse


class CreationRepository:
    def upsert_story_draft(
        self,
        project_id: str,
        title: str,
        opening: str,
        body: str,
        closing: str,
    ) -> StoryDraftResponse:
        with SessionLocal() as session:
            model = session.execute(
                select(StoryDraftModel).where(StoryDraftModel.project_id == project_id),
            ).scalar_one_or_none()
            if model is None:
                model = StoryDraftModel(
                    project_id=project_id,
                    title=title,
                    opening=opening,
                    body=body,
                    closing=closing,
                    updated_at=now_iso(),
                )
                session.add(model)
            else:
                model.title = title
                model.opening = opening
                model.body = body
                model.closing = closing
                model.updated_at = now_iso()
            session.commit()
            session.refresh(model)
            return self._to_story_draft(model)

    def get_story_draft(self, project_id: str) -> StoryDraftResponse | None:
        with SessionLocal() as session:
            model = session.execute(
                select(StoryDraftModel).where(StoryDraftModel.project_id == project_id),
            ).scalar_one_or_none()
            return self._to_story_draft(model) if model else None

    def replace_storyboard(
        self,
        project_id: str,
        sections: list[dict],
    ) -> list[StoryboardSectionResponse]:
        with SessionLocal() as session:
            session.execute(delete(StoryboardSectionModel).where(StoryboardSectionModel.project_id == project_id))
            models: list[StoryboardSectionModel] = []
            for index, section in enumerate(sections):
                model = StoryboardSectionModel(
                    section_id=section["section_id"],
                    project_id=project_id,
                    order_index=index,
                    title=section["title"],
                    summary=section["summary"],
                    subtitle_line=section["subtitle_line"],
                    duration_label=section["duration_label"],
                )
                session.add(model)
                models.append(model)
            session.commit()
            return [self._to_storyboard(item) for item in models]

    def get_storyboard(self, project_id: str) -> list[StoryboardSectionResponse]:
        with SessionLocal() as session:
            models = session.execute(
                select(StoryboardSectionModel)
                .where(StoryboardSectionModel.project_id == project_id)
                .order_by(StoryboardSectionModel.order_index.asc()),
            ).scalars().all()
            return [self._to_storyboard(item) for item in models]

    def upsert_preview(
        self,
        project_id: str,
        title: str,
        subtitle_summary: str,
        music_label: str,
        cover_caption: str,
        video_url: str,
        cover_url: str,
    ) -> PreviewAssetResponse:
        with SessionLocal() as session:
            model = session.execute(
                select(PreviewAssetModel).where(PreviewAssetModel.project_id == project_id),
            ).scalar_one_or_none()
            if model is None:
                model = PreviewAssetModel(
                    project_id=project_id,
                    title=title,
                    subtitle_summary=subtitle_summary,
                    music_label=music_label,
                    cover_caption=cover_caption,
                    video_url=video_url,
                    cover_url=cover_url,
                    updated_at=now_iso(),
                )
                session.add(model)
            else:
                model.title = title
                model.subtitle_summary = subtitle_summary
                model.music_label = music_label
                model.cover_caption = cover_caption
                model.video_url = video_url
                model.cover_url = cover_url
                model.updated_at = now_iso()
            session.commit()
            session.refresh(model)
            return self._to_preview(model)

    def get_preview(self, project_id: str) -> PreviewAssetResponse | None:
        with SessionLocal() as session:
            model = session.execute(
                select(PreviewAssetModel).where(PreviewAssetModel.project_id == project_id),
            ).scalar_one_or_none()
            return self._to_preview(model) if model else None

    def create_or_update_task(
        self,
        task_id: str,
        project_id: str,
        task_type: str,
        status: str,
        progress: int,
        current_stage: str,
        estimated_remaining_seconds: int,
        result_json: str | None = None,
        error_json: str | None = None,
    ) -> TaskStatusResponse:
        with SessionLocal() as session:
            model = session.execute(select(TaskModel).where(TaskModel.task_id == task_id)).scalar_one_or_none()
            if model is None:
                model = TaskModel(
                    task_id=task_id,
                    project_id=project_id,
                    task_type=task_type,
                    status=status,
                    progress=progress,
                    current_stage=current_stage,
                    estimated_remaining_seconds=estimated_remaining_seconds,
                    result_json=result_json,
                    error_json=error_json,
                    updated_at=now_iso(),
                )
                session.add(model)
            else:
                model.project_id = project_id
                model.task_type = task_type
                model.status = status
                model.progress = progress
                model.current_stage = current_stage
                model.estimated_remaining_seconds = estimated_remaining_seconds
                model.result_json = result_json
                model.error_json = error_json
                model.updated_at = now_iso()
            session.commit()
            session.refresh(model)
            return TaskStatusResponse(
                taskId=model.task_id,
                status=model.status,
                progress=model.progress,
                currentStage=model.current_stage,
                estimatedRemainingSeconds=model.estimated_remaining_seconds,
                result=None,
                error=None,
                updatedAt=model.updated_at,
            )

    @staticmethod
    def _to_story_draft(model: StoryDraftModel) -> StoryDraftResponse:
        return StoryDraftResponse(
            projectId=model.project_id,
            title=model.title,
            opening=model.opening,
            body=model.body,
            closing=model.closing,
            updatedAt=model.updated_at,
        )

    @staticmethod
    def _to_storyboard(model: StoryboardSectionModel) -> StoryboardSectionResponse:
        return StoryboardSectionResponse(
            sectionId=model.section_id,
            projectId=model.project_id,
            orderIndex=model.order_index,
            title=model.title,
            summary=model.summary,
            subtitleLine=model.subtitle_line,
            durationLabel=model.duration_label,
        )

    @staticmethod
    def _to_preview(model: PreviewAssetModel) -> PreviewAssetResponse:
        return PreviewAssetResponse(
            projectId=model.project_id,
            title=model.title,
            subtitleSummary=model.subtitle_summary,
            musicLabel=model.music_label,
            coverCaption=model.cover_caption,
            videoUrl=model.video_url,
            coverUrl=model.cover_url,
            updatedAt=model.updated_at,
        )

