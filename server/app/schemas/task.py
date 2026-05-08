from pydantic import BaseModel, ConfigDict, Field


class TaskStatusResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    task_id: str = Field(alias="taskId")
    status: str
    progress: int
    current_stage: str = Field(alias="currentStage")
    estimated_remaining_seconds: int = Field(alias="estimatedRemainingSeconds")
    result: dict | None = None
    error: dict | None = None
    updated_at: str = Field(alias="updatedAt")
