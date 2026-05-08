from pydantic import BaseModel, ConfigDict, Field


class UploadInitiateRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    file_name: str = Field(alias="fileName")
    mime_type: str = Field(alias="mimeType")
    size_bytes: int = Field(alias="sizeBytes")
    sha256: str
    project_id: str = Field(alias="projectId")


class UploadInitiateResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    upload_id: str = Field(alias="uploadId")
    storage_provider: str = Field(alias="storageProvider")
    bucket: str
    object_key: str = Field(alias="objectKey")
    upload_mode: str = Field(alias="uploadMode")
    part_size_bytes: int = Field(alias="partSizeBytes")
    already_uploaded: bool = Field(alias="alreadyUploaded")
    presigned_urls: list[str] = Field(alias="presignedUrls")


class UploadStatusResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    upload_id: str = Field(alias="uploadId")
    status: str
    uploaded_part_count: int = Field(alias="uploadedPartCount")
    total_part_count: int = Field(alias="totalPartCount")
    deduplicated: bool
    material_id: str | None = Field(alias="materialId", default=None)
