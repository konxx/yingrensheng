import json

from sqlalchemy import select

from app.db.models import TaskModel
from app.db.session import SessionLocal
from app.schemas.task import TaskStatusResponse


class TaskRepository:
    def upsert(
        self,
        task_id: str,
        project_id: str | None,
        task_type: str,
        status: str,
        progress: int,
        current_stage: str,
        estimated_remaining_seconds: int,
        result_json: str | None = None,
        error_json: str | None = None,
        updated_at: str | None = None,
    ) -> TaskStatusResponse:
        from app.core.time import now_iso

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
                    updated_at=updated_at or now_iso(),
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
                model.updated_at = updated_at or now_iso()
            session.commit()
            session.refresh(model)
            return self._to_schema(model)

    def get(self, task_id: str) -> TaskStatusResponse | None:
        with SessionLocal() as session:
            stmt = select(TaskModel).where(TaskModel.task_id == task_id)
            model = session.execute(stmt).scalar_one_or_none()
            return self._to_schema(model) if model else None

    @staticmethod
    def _to_schema(model: TaskModel) -> TaskStatusResponse:
        return TaskStatusResponse(
            taskId=model.task_id,
            status=model.status,
            progress=model.progress,
            currentStage=model.current_stage,
            estimatedRemainingSeconds=model.estimated_remaining_seconds,
            result=json.loads(model.result_json) if model.result_json else None,
            error=json.loads(model.error_json) if model.error_json else None,
            updatedAt=model.updated_at,
        )
