from fastapi import APIRouter, HTTPException

from app.schemas.common import ApiResponse
from app.schemas.task import TaskStatusResponse
from app.services.repositories import service_container


router = APIRouter()


@router.get("/{task_id}", response_model=ApiResponse[TaskStatusResponse])
def get_task(task_id: str) -> ApiResponse[TaskStatusResponse]:
    task = service_container.task_service.get_task(task_id)
    if task is None:
        raise HTTPException(status_code=404, detail="TASK_NOT_FOUND")
    return ApiResponse.success(data=task, request_id="req_task_detail")

