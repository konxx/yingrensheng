from fastapi import APIRouter, File, Form, HTTPException, UploadFile

from app.schemas.common import ApiResponse
from app.schemas.material import MaterialResponse
from app.schemas.upload import UploadInitiateRequest, UploadInitiateResponse, UploadStatusResponse
from app.services.container import service_container


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


@router.post("/files", response_model=ApiResponse[list[MaterialResponse]])
def upload_files(
    project_id: str = Form(alias="projectId"),
    files: list[UploadFile] = File(...),
) -> ApiResponse[list[MaterialResponse]]:
    materials = service_container.material_service.save_uploads(project_id=project_id, files=files)
    return ApiResponse.success(data=materials, request_id="req_upload_files")
