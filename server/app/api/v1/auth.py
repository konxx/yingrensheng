from fastapi import APIRouter

from app.schemas.auth import LoginRequest, LoginResponseData, SendSmsRequest
from app.schemas.common import ApiResponse
from app.services.container import service_container


router = APIRouter()


@router.post("/sms/send", response_model=ApiResponse[dict[str, str]])
def send_sms(payload: SendSmsRequest) -> ApiResponse[dict[str, str]]:
    service_container.auth_service.send_sms(payload.phone, payload.purpose)
    return ApiResponse.success(data={"status": "sent"}, request_id="req_auth_sms_send")


@router.post("/login", response_model=ApiResponse[LoginResponseData])
def login(payload: LoginRequest) -> ApiResponse[LoginResponseData]:
    data = service_container.auth_service.login(
        phone=payload.phone,
        sms_code=payload.sms_code,
        device_id=payload.device_id,
    )
    return ApiResponse.success(data=data, request_id="req_auth_login")
