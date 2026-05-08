from fastapi import APIRouter

from app.schemas.common import ApiResponse
from app.schemas.user import MemberMeResponse
from app.services.container import service_container


router = APIRouter()


@router.get("/me", response_model=ApiResponse[MemberMeResponse])
def get_member_me() -> ApiResponse[MemberMeResponse]:
    return ApiResponse.success(
        data=service_container.user_service.get_member_info(),
        request_id="req_member_me",
    )
