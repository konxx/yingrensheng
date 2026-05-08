from fastapi import APIRouter, HTTPException

from app.schemas.common import ApiResponse, PageResponse
from app.schemas.project import CreateProjectRequest, ProjectDetail
from app.services.repositories import service_container


router = APIRouter()


@router.post("", response_model=ApiResponse[ProjectDetail])
def create_project(payload: CreateProjectRequest) -> ApiResponse[ProjectDetail]:
    project = service_container.project_service.create_project(payload)
    return ApiResponse.success(data=project, request_id="req_project_create")


@router.get("", response_model=ApiResponse[PageResponse[ProjectDetail]])
def list_projects(status: str | None = None, page: int = 1, page_size: int = 20) -> ApiResponse[PageResponse[ProjectDetail]]:
    data = service_container.project_service.list_projects(
        status=status,
        page=page,
        page_size=page_size,
    )
    return ApiResponse.success(data=data, request_id="req_project_list")


@router.get("/{project_id}", response_model=ApiResponse[ProjectDetail])
def get_project(project_id: str) -> ApiResponse[ProjectDetail]:
    project = service_container.project_service.get_project(project_id)
    if project is None:
        raise HTTPException(status_code=404, detail="PROJECT_NOT_FOUND")
    return ApiResponse.success(data=project, request_id="req_project_detail")

