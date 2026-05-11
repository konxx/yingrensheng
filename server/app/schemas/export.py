from pydantic import BaseModel, ConfigDict, Field


class ExportGenerateRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    export_plan_id: str = Field(alias="exportPlanId")
    resolution: str
    remove_watermark: bool = Field(alias="removeWatermark")

