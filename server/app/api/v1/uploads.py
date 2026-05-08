from fastapi import APIRouter, HTTPException

from app.schemas.common import ApiResponse
from app.schemas.upload import UploadInitiateRequest, UploadInitiateResponse, UploadStatusResponse
from app.services.repositories import service_container


router = APIRouter()


@router.post("/initiate", response_model=ApiResponse[UploadInitiateResponse])
def initiate_upload(payload: UploadInitiateRequest) -> ApiResponse[UploadInitiateResponse]:
    data = service_container.upload_service.initiate_upload(payload)
    return ApiResponse.success(data=data, request_id="req_upload_initiate")


@router.get("/{upload_id}", response_model=ApiResponse[UploadStatusResponse])
def get_upload(upload_id: str) -> ApiResponse[UploadStatusResponse]:
    upload = service_container.upload_service.get_upload(upload_id)
    if upload is None:
        raise HTTPException(status_code=404, detail="UPLOAD_NOT_FOUND")
    return ApiResponse.success(data=upload, request_id="req_upload_detail")

