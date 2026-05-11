from fastapi import APIRouter

from app.schemas.common import ApiResponse
from app.schemas.work import WorkResponse
from app.services.container import service_container


router = APIRouter()


@router.get("", response_model=ApiResponse[list[WorkResponse]])
def list_works() -> ApiResponse[list[WorkResponse]]:
    return ApiResponse.success(
        data=service_container.work_service.list_works(),
        request_id="req_works_list",
    )
