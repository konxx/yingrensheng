from pydantic import BaseModel, ConfigDict, Field


class MaterialResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    material_id: str = Field(alias="materialId")
    project_id: str = Field(alias="projectId")
    title: str
    material_type: str = Field(alias="materialType")
    duration_label: str = Field(alias="durationLabel")
    insight: str
    local_path: str = Field(alias="localPath")
    mime_type: str = Field(alias="mimeType")
    size_bytes: int = Field(alias="sizeBytes")
    status: str
    created_at: str = Field(alias="createdAt")
    updated_at: str = Field(alias="updatedAt")
