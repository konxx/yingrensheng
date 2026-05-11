from pydantic import BaseModel, ConfigDict, Field


class MemberPlanResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    plan_id: str = Field(alias="planId")
    name: str
    monthly_price: int = Field(alias="monthlyPrice")
    badge: str | None = None
    summary: str
    features: list[str]

