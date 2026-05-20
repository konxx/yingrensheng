from fastapi import APIRouter, HTTPException

from app.schemas.common import ApiResponse
from app.schemas.work import WorkAssetResponse, WorkResponse
from app.services.container import service_container


router = APIRouter()


@router.get("", response_model=ApiResponse[list[WorkResponse]])
def list_works() -> ApiResponse[list[WorkResponse]]:
    return ApiResponse.success(
        data=service_container.work_service.list_works(),
        request_id="req_works_list",
    )


@router.get("/{work_id}/assets", response_model=ApiResponse[list[WorkAssetResponse]])
def list_work_assets(work_id: str) -> ApiResponse[list[WorkAssetResponse]]:
    work = service_container.work_service.get_work(work_id)
    if work is None:
        raise HTTPException(status_code=404, detail="WORK_NOT_FOUND")
    return ApiResponse.success(
        data=service_container.work_service.list_work_assets(work_id),
        request_id="req_work_assets",
    )


@router.delete("/{work_id}", response_model=ApiResponse[dict[str, bool]])
def delete_work(work_id: str) -> ApiResponse[dict[str, bool]]:
    deleted = service_container.work_service.delete_work(work_id)
    return ApiResponse.success(
        data={"deleted": deleted},
        request_id="req_works_delete",
    )
