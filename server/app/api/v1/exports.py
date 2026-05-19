from fastapi import APIRouter, Header, HTTPException

from app.schemas.common import ApiResponse
from app.schemas.export import ExportGenerateRequest
from app.schemas.task import TaskStatusResponse
from app.services.container import service_container


router = APIRouter()


@router.post("/{project_id}", response_model=ApiResponse[TaskStatusResponse])
def export_project(
    project_id: str,
    payload: ExportGenerateRequest,
    authorization: str | None = Header(default=None),
) -> ApiResponse[TaskStatusResponse]:
    try:
        task = service_container.creation_service.export_project(
            project_id=project_id,
            payload=payload,
            token=authorization,
        )
    except ValueError as exc:
        code = str(exc)
        status_code = {
            "PROJECT_NOT_FOUND": 404,
            "EXPORT_PREREQUISITES_MISSING": 409,
            "ADMIN_EXPORT_REQUIRES_ADMIN": 403,
        }.get(code, 400)
        raise HTTPException(status_code=status_code, detail=code) from exc
    return ApiResponse.success(data=task, request_id="req_export_create")
