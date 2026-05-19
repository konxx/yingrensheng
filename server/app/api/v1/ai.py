from fastapi import APIRouter

from app.schemas.common import ApiResponse
from app.services.container import service_container


router = APIRouter()


@router.get("/status", response_model=ApiResponse[dict])
def get_ai_status() -> ApiResponse[dict]:
    return ApiResponse.success(
        data=service_container.text_client.provider.status(),
        request_id="req_ai_status",
    )
