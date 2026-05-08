from pydantic import BaseModel, ConfigDict, Field


class UserProfile(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    user_id: str = Field(alias="userId")
    nickname: str
    phone: str
    avatar_url: str = Field(alias="avatarUrl", default="")


class MemberMeResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    level_name: str = Field(alias="levelName")
    subtitle: str
    remaining_export_count: int = Field(alias="remainingExportCount")
    active_plan_price_label: str = Field(alias="activePlanPriceLabel")
