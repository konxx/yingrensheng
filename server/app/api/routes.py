from fastapi import APIRouter

from app.api.v1.ai import router as ai_router
from app.api.v1.auth import router as auth_router
from app.api.v1.creation import router as creation_router
from app.api.v1.exports import router as exports_router
from app.api.v1.member import router as member_router
from app.api.v1.materials import router as materials_router
from app.api.v1.orders import router as orders_router
from app.api.v1.projects import router as projects_router
from app.api.v1.scenes import router as scenes_router
from app.api.v1.tasks import router as tasks_router
from app.api.v1.uploads import router as uploads_router
from app.api.v1.users import router as users_router
from app.api.v1.works import router as works_router


api_router = APIRouter()
api_router.include_router(ai_router, prefix="/ai", tags=["ai"])
api_router.include_router(auth_router, prefix="/auth", tags=["auth"])
api_router.include_router(creation_router, prefix="/creation", tags=["creation"])
api_router.include_router(exports_router, prefix="/exports", tags=["exports"])
api_router.include_router(users_router, prefix="/users", tags=["users"])
api_router.include_router(member_router, prefix="/member", tags=["member"])
api_router.include_router(materials_router, prefix="/materials", tags=["materials"])
api_router.include_router(orders_router, prefix="/orders", tags=["orders"])
api_router.include_router(scenes_router, prefix="/scenes", tags=["scenes"])
api_router.include_router(projects_router, prefix="/projects", tags=["projects"])
api_router.include_router(uploads_router, prefix="/uploads", tags=["uploads"])
api_router.include_router(tasks_router, prefix="/tasks", tags=["tasks"])
api_router.include_router(works_router, prefix="/works", tags=["works"])
