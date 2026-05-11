from fastapi import APIRouter

from app.schemas.common import ApiResponse
from app.schemas.export import ExportGenerateRequest
from app.schemas.task import TaskStatusResponse
from app.services.container import service_container


router = APIRouter()


@router.post("/{project_id}", response_model=ApiResponse[TaskStatusResponse])
def export_project(project_id: str, payload: ExportGenerateRequest) -> ApiResponse[TaskStatusResponse]:
    task = service_container.creation_service.export_project(project_id, payload)
    return ApiResponse.success(data=task, request_id="req_export_create")
