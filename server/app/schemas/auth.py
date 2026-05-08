from pydantic import BaseModel, ConfigDict, Field

from app.schemas.user import UserProfile


class SendSmsRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    phone: str
    purpose: str


class LoginRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    phone: str
    sms_code: str = Field(alias="smsCode")
    device_id: str = Field(alias="deviceId")


class LoginResponseData(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    access_token: str = Field(alias="accessToken")
    refresh_token: str = Field(alias="refreshToken")
    expires_in_seconds: int = Field(alias="expiresInSeconds")
    user: UserProfile
