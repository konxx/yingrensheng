from fastapi import APIRouter

from app.schemas.common import ApiResponse
from app.schemas.scene import SceneListItem
from app.services.repositories import service_container


router = APIRouter()


@router.get("", response_model=ApiResponse[list[SceneListItem]])
def list_scenes() -> ApiResponse[list[SceneListItem]]:
    return ApiResponse.success(
        data=service_container.scene_service.list_scenes(),
        request_id="req_scenes_list",
    )

