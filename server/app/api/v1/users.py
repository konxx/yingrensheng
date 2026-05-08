from fastapi import APIRouter

from app.schemas.common import ApiResponse
from app.schemas.user import UserProfile
from app.services.container import service_container


router = APIRouter()


@router.get("/me", response_model=ApiResponse[UserProfile])
def get_me() -> ApiResponse[UserProfile]:
    return ApiResponse.success(
        data=service_container.user_service.get_current_user(),
        request_id="req_users_me",
    )
