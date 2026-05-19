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


@router.delete("/{work_id}", response_model=ApiResponse[dict[str, bool]])
def delete_work(work_id: str) -> ApiResponse[dict[str, bool]]:
    deleted = service_container.work_service.delete_work(work_id)
    return ApiResponse.success(
        data={"deleted": deleted},
        request_id="req_works_delete",
    )
