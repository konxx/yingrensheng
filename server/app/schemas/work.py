from pydantic import BaseModel, ConfigDict, Field


class WorkResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    work_id: str = Field(alias="workId")
    project_id: str = Field(alias="projectId")
    title: str
    scene_label: str = Field(alias="sceneLabel")
    duration_label: str = Field(alias="durationLabel")
    status_label: str = Field(alias="statusLabel")
    updated_at: str = Field(alias="updatedAt")
    cover_url: str = Field(alias="coverUrl")
    video_url: str = Field(alias="videoUrl")

