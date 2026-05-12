from fastapi import APIRouter, HTTPException

from app.schemas.creation import (
    PreviewAssetResponse,
    StoryDraftGenerateRequest,
    StoryDraftResponse,
    StoryboardSectionResponse,
)
from app.schemas.common import ApiResponse, PageResponse
from app.schemas.material import MaterialResponse
from app.schemas.project import CreateProjectRequest, ProjectDetail
from app.schemas.task import TaskStatusResponse
from app.services.container import service_container


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


@router.get("/{project_id}/materials", response_model=ApiResponse[list[MaterialResponse]])
def get_project_materials(project_id: str) -> ApiResponse[list[MaterialResponse]]:
    materials = service_container.material_service.list_materials(project_id)
    return ApiResponse.success(data=materials, request_id="req_project_materials")


@router.post("/{project_id}/story-draft/generate", response_model=ApiResponse[TaskStatusResponse])
def generate_story_draft(project_id: str, payload: StoryDraftGenerateRequest) -> ApiResponse[TaskStatusResponse]:
    try:
        task = service_container.creation_service.generate_story_draft(
            project_id=project_id,
            style_id=payload.style_id,
            theme_line=payload.theme_line,
        )
    except ValueError as exc:
        raise _project_http_exception(exc) from exc
    return ApiResponse.success(data=task, request_id="req_story_draft_generate")


@router.get("/{project_id}/story-draft", response_model=ApiResponse[StoryDraftResponse])
def get_story_draft(project_id: str) -> ApiResponse[StoryDraftResponse]:
    draft = service_container.creation_service.get_story_draft(project_id)
    if draft is None:
        raise HTTPException(status_code=404, detail="STORY_DRAFT_NOT_FOUND")
    return ApiResponse.success(data=draft, request_id="req_story_draft_detail")


@router.post("/{project_id}/storyboard/generate", response_model=ApiResponse[TaskStatusResponse])
def generate_storyboard(project_id: str) -> ApiResponse[TaskStatusResponse]:
    try:
        task = service_container.creation_service.generate_storyboard(project_id)
    except ValueError as exc:
        raise _project_http_exception(exc) from exc
    return ApiResponse.success(data=task, request_id="req_storyboard_generate")


@router.get("/{project_id}/storyboard", response_model=ApiResponse[list[StoryboardSectionResponse]])
def get_storyboard(project_id: str) -> ApiResponse[list[StoryboardSectionResponse]]:
    sections = service_container.creation_service.get_storyboard(project_id)
    return ApiResponse.success(data=sections, request_id="req_storyboard_detail")


@router.post("/{project_id}/preview/generate", response_model=ApiResponse[TaskStatusResponse])
def generate_preview(project_id: str) -> ApiResponse[TaskStatusResponse]:
    try:
        task = service_container.creation_service.generate_preview(project_id)
    except ValueError as exc:
        raise _project_http_exception(exc) from exc
    return ApiResponse.success(data=task, request_id="req_preview_generate")


@router.get("/{project_id}/preview", response_model=ApiResponse[PreviewAssetResponse])
def get_preview(project_id: str) -> ApiResponse[PreviewAssetResponse]:
    preview = service_container.creation_service.get_preview(project_id)
    if preview is None:
        raise HTTPException(status_code=404, detail="PREVIEW_NOT_FOUND")
    return ApiResponse.success(data=preview, request_id="req_preview_detail")


def _project_http_exception(exc: ValueError) -> HTTPException:
    code = str(exc)
    status_code = {
        "PROJECT_NOT_FOUND": 404,
        "PREVIEW_PREREQUISITES_MISSING": 409,
        "EXPORT_PREREQUISITES_MISSING": 409,
    }.get(code, 400)
    return HTTPException(status_code=status_code, detail=code)
