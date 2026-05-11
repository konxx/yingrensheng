from fastapi import APIRouter

from app.schemas.common import ApiResponse
from app.schemas.member import MemberPlanResponse
from app.schemas.user import MemberMeResponse
from app.services.container import service_container


router = APIRouter()


@router.get("/me", response_model=ApiResponse[MemberMeResponse])
def get_member_me() -> ApiResponse[MemberMeResponse]:
    return ApiResponse.success(
        data=service_container.user_service.get_member_info(),
        request_id="req_member_me",
    )


@router.get("/plans", response_model=ApiResponse[list[MemberPlanResponse]])
def get_member_plans() -> ApiResponse[list[MemberPlanResponse]]:
    return ApiResponse.success(
        data=service_container.member_service.list_plans(),
        request_id="req_member_plans",
    )
