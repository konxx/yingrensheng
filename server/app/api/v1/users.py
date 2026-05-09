from fastapi import APIRouter, HTTPException

from app.schemas.common import ApiResponse
from app.schemas.user import UpdateUserProfileRequest, UserProfile
from app.services.container import service_container


router = APIRouter()


@router.get("/me", response_model=ApiResponse[UserProfile])
def get_me() -> ApiResponse[UserProfile]:
    return ApiResponse.success(
        data=service_container.user_service.get_current_user(),
        request_id="req_users_me",
    )


@router.post("/profile/update", response_model=ApiResponse[UserProfile])
def update_profile(payload: UpdateUserProfileRequest) -> ApiResponse[UserProfile]:
    try:
        profile = service_container.user_service.update_profile(payload)
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc))
    return ApiResponse.success(data=profile, request_id="req_users_profile_update")
