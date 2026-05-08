from fastapi import APIRouter, HTTPException

from app.schemas.auth import LoginRequest, LoginResponseData, RegisterRequest, SendSmsRequest
from app.schemas.common import ApiResponse
from app.services.container import service_container


router = APIRouter()


@router.post("/sms/send", response_model=ApiResponse[dict[str, str]])
def send_sms(payload: SendSmsRequest) -> ApiResponse[dict[str, str]]:
    service_container.auth_service.send_sms(payload.phone, payload.purpose)
    return ApiResponse.success(data={"status": "sent"}, request_id="req_auth_sms_send")


@router.post("/login", response_model=ApiResponse[LoginResponseData])
def login(payload: LoginRequest) -> ApiResponse[LoginResponseData]:
    try:
        data = service_container.auth_service.login(
            phone=payload.phone,
            sms_code=payload.sms_code,
            device_id=payload.device_id,
            password=payload.password,
        )
    except ValueError as exc:
        raise HTTPException(status_code=401, detail=str(exc))
    return ApiResponse.success(data=data, request_id="req_auth_login")


@router.post("/register", response_model=ApiResponse[LoginResponseData])
def register(payload: RegisterRequest) -> ApiResponse[LoginResponseData]:
    try:
        data = service_container.auth_service.register(payload)
    except ValueError as exc:
        raise HTTPException(status_code=409, detail=str(exc))
    return ApiResponse.success(data=data, request_id="req_auth_register")
