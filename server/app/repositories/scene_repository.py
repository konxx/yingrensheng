import json

from sqlalchemy import select

from app.db.models import SceneModel
from app.db.session import SessionLocal
from app.schemas.scene import SceneListItem


class SceneRepository:
    def list(self) -> list[SceneListItem]:
        with SessionLocal() as session:
            stmt = select(SceneModel).order_by(SceneModel.id.asc())
            models = session.execute(stmt).scalars().all()
            return [
                SceneListItem(
                    sceneId=model.scene_id,
                    title=model.title,
                    subtitle=model.subtitle,
                    recommendedDurationLabel=model.recommended_duration_label,
                    estimatedTimeLabel=model.estimated_time_label,
                    coverUrl=model.cover_url,
                    supportedMaterialTypes=json.loads(model.supported_material_types_json),
                )
                for model in models
            ]

