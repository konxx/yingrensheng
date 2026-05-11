from fastapi import APIRouter

from app.schemas.common import ApiResponse
from app.schemas.order import OrderResponse
from app.services.container import service_container


router = APIRouter()


@router.get("", response_model=ApiResponse[list[OrderResponse]])
def list_orders() -> ApiResponse[list[OrderResponse]]:
    return ApiResponse.success(
        data=service_container.order_service.list_orders(),
        request_id="req_orders_list",
    )

