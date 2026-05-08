from pydantic import BaseModel, ConfigDict, Field


class CreateProjectRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    scene_id: str = Field(alias="sceneId")
    mode: str
    title: str


class ProjectDetail(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    project_id: str = Field(alias="projectId")
    scene_id: str = Field(alias="sceneId")
    scene_title: str = Field(alias="sceneTitle")
    mode: str
    title: str
    status: str
    progress: int
    current_step: str = Field(alias="currentStep")
    material_count: int = Field(alias="materialCount")
    updated_at: str = Field(alias="updatedAt")
