from pydantic import BaseModel, ConfigDict, Field


class InterviewPromptResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    prompt_id: str = Field(alias="promptId")
    title: str
    helper: str


class StoryDraftGenerateRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    style_id: str = Field(alias="styleId")
    theme_line: str = Field(alias="themeLine")


class StoryDraftResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    project_id: str = Field(alias="projectId")
    title: str
    opening: str
    body: str
    closing: str
    updated_at: str = Field(alias="updatedAt")


class StoryboardSectionResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    section_id: str = Field(alias="sectionId")
    project_id: str = Field(alias="projectId")
    order_index: int = Field(alias="orderIndex")
    title: str
    summary: str
    subtitle_line: str = Field(alias="subtitleLine")
    duration_label: str = Field(alias="durationLabel")


class PreviewAssetResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    project_id: str = Field(alias="projectId")
    title: str
    subtitle_summary: str = Field(alias="subtitleSummary")
    music_label: str = Field(alias="musicLabel")
    cover_caption: str = Field(alias="coverCaption")
    video_url: str = Field(alias="videoUrl")
    cover_url: str = Field(alias="coverUrl")
    updated_at: str = Field(alias="updatedAt")

