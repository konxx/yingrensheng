from fastapi import APIRouter

from app.schemas.common import ApiResponse
from app.schemas.creation import InterviewPromptResponse
from app.services.container import service_container


router = APIRouter()


@router.get("/interview-prompts", response_model=ApiResponse[list[InterviewPromptResponse]])
def interview_prompts(scene_id: str | None = None) -> ApiResponse[list[InterviewPromptResponse]]:
    return ApiResponse.success(
        data=service_container.creation_service.get_interview_prompts(scene_id=scene_id),
        request_id="req_creation_prompts",
    )

