from sqlalchemy import select

from app.core.time import now_iso
from app.db.models import ProjectModel
from app.db.session import SessionLocal
from app.schemas.common import PageResponse
from app.schemas.project import CreateProjectRequest, ProjectDetail


class ProjectRepository:
    def create(self, payload: CreateProjectRequest, scene_title: str) -> ProjectDetail:
        with SessionLocal() as session:
            model = ProjectModel(
                project_id=f"project_{session.query(ProjectModel).count() + 1:03d}",
                scene_id=payload.scene_id,
                scene_title=scene_title,
                mode=payload.mode,
                title=payload.title,
                status="DRAFT",
                progress=8,
                current_step="已创建草稿",
                material_count=0,
                updated_at=now_iso(),
            )
            session.add(model)
            session.commit()
            session.refresh(model)
            return self._to_schema(model)

    def list(self, status: str | None, page: int, page_size: int) -> PageResponse[ProjectDetail]:
        with SessionLocal() as session:
            stmt = select(ProjectModel).order_by(ProjectModel.id.desc())
            if status:
                stmt = stmt.where(ProjectModel.status == status)
            all_items = session.execute(stmt).scalars().all()
            start = max(page - 1, 0) * page_size
            sliced = all_items[start : start + page_size]
            return PageResponse[ProjectDetail](
                items=[self._to_schema(item) for item in sliced],
                page=page,
                pageSize=page_size,
                hasMore=start + page_size < len(all_items),
                nextCursor=None,
            )

    def get(self, project_id: str) -> ProjectDetail | None:
        with SessionLocal() as session:
            stmt = select(ProjectModel).where(ProjectModel.project_id == project_id)
            model = session.execute(stmt).scalar_one_or_none()
            return self._to_schema(model) if model else None

    @staticmethod
    def _to_schema(model: ProjectModel) -> ProjectDetail:
        return ProjectDetail(
            projectId=model.project_id,
            sceneId=model.scene_id,
            sceneTitle=model.scene_title,
            mode=model.mode,
            title=model.title,
            status=model.status,
            progress=model.progress,
            currentStep=model.current_step,
            materialCount=model.material_count,
            updatedAt=model.updated_at,
        )

