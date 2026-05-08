from __future__ import annotations

from typing import Generic, TypeVar

from pydantic import BaseModel, ConfigDict, Field


T = TypeVar("T")


class ErrorResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    code: str
    message: str
    details: dict = Field(default_factory=dict)


class ApiResponse(BaseModel, Generic[T]):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    request_id: str = Field(alias="requestId")
    data: T | None = None
    error: ErrorResponse | None = None

    @classmethod
    def success(cls, data: T, request_id: str) -> "ApiResponse[T]":
        return cls(requestId=request_id, data=data)


class PageResponse(BaseModel, Generic[T]):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    items: list[T]
    page: int
    page_size: int = Field(alias="pageSize")
    has_more: bool = Field(alias="hasMore")
    next_cursor: str | None = Field(default=None, alias="nextCursor")
