import json

from sqlalchemy import select

from app.db.models import TaskModel
from app.db.session import SessionLocal
from app.schemas.task import TaskStatusResponse


class TaskRepository:
    def get(self, task_id: str) -> TaskStatusResponse | None:
        with SessionLocal() as session:
            stmt = select(TaskModel).where(TaskModel.task_id == task_id)
            model = session.execute(stmt).scalar_one_or_none()
            if model is None:
                return None
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

