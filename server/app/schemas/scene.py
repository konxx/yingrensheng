from pydantic import BaseModel, ConfigDict, Field


class SceneListItem(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    scene_id: str = Field(alias="sceneId")
    title: str
    subtitle: str
    recommended_duration_label: str = Field(alias="recommendedDurationLabel")
    estimated_time_label: str = Field(alias="estimatedTimeLabel")
    cover_url: str = Field(alias="coverUrl")
    supported_material_types: list[str] = Field(alias="supportedMaterialTypes")
