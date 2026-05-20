from __future__ import annotations

import json
import uuid

from sqlalchemy import delete, select

from app.core.time import now_iso
from app.db.models import WorkAssetModel, WorkModel
from app.db.session import SessionLocal
from app.schemas.work import WorkAssetResponse, WorkResponse


def _output_kind_from_work(scene_label: str, duration_label: str, video_url: str) -> str:
    if video_url or "视频" in scene_label or "视频" in duration_label:
        return "SHORT_VIDEO"
    if "漫画" in scene_label or "连环" in scene_label or "格漫画" in duration_label:
        return "COMIC_STORYBOARD"
    if "角色" in scene_label or "西游记" in scene_label or "红楼梦" in scene_label:
        return "CHARACTER_STORY"
    return "STORY_TEXT"


class WorkRepository:
    def list(self) -> list[WorkResponse]:
        with SessionLocal() as session:
            stmt = select(WorkModel).order_by(WorkModel.id.desc())
            models = session.execute(stmt).scalars().all()
            return [
                WorkResponse(
                    workId=model.work_id,
                    projectId=model.project_id,
                    title=model.title,
                    sceneLabel=model.scene_label,
                    durationLabel=model.duration_label,
                    statusLabel=model.status_label,
                    updatedAt=model.updated_at,
                    coverUrl=model.cover_url,
                    videoUrl=model.video_url,
                    outputKind=_output_kind_from_work(
                        scene_label=model.scene_label,
                        duration_label=model.duration_label,
                        video_url=model.video_url,
                    ),
                )
                for model in models
            ]

    def create_or_update(
        self,
        project_id: str,
        title: str,
        scene_label: str,
        duration_label: str,
        status_label: str,
        cover_url: str,
        video_url: str,
    ) -> WorkResponse:
        with SessionLocal() as session:
            model = session.execute(
                select(WorkModel).where(WorkModel.project_id == project_id),
            ).scalar_one_or_none()
            if model is None:
                model = WorkModel(
                    work_id=f"work_{uuid.uuid4().hex[:12]}",
                    project_id=project_id,
                    title=title,
                    scene_label=scene_label,
                    duration_label=duration_label,
                    status_label=status_label,
                    updated_at=now_iso(),
                    cover_url=cover_url,
                    video_url=video_url,
                )
                session.add(model)
            else:
                model.title = title
                model.scene_label = scene_label
                model.duration_label = duration_label
                model.status_label = status_label
                model.cover_url = cover_url
                model.video_url = video_url
                model.updated_at = now_iso()
            session.commit()
            session.refresh(model)
            return WorkResponse(
                workId=model.work_id,
                projectId=model.project_id,
                title=model.title,
                sceneLabel=model.scene_label,
                durationLabel=model.duration_label,
                statusLabel=model.status_label,
                updatedAt=model.updated_at,
                coverUrl=model.cover_url,
                videoUrl=model.video_url,
                outputKind=_output_kind_from_work(
                    scene_label=model.scene_label,
                    duration_label=model.duration_label,
                    video_url=model.video_url,
                ),
            )

    def get(self, work_id: str) -> WorkResponse | None:
        with SessionLocal() as session:
            model = session.execute(
                select(WorkModel).where(WorkModel.work_id == work_id),
            ).scalar_one_or_none()
            return self._to_work(model) if model else None

    def get_by_project_id(self, project_id: str) -> WorkResponse | None:
        with SessionLocal() as session:
            model = session.execute(
                select(WorkModel).where(WorkModel.project_id == project_id),
            ).scalar_one_or_none()
            return self._to_work(model) if model else None

    def replace_assets(
        self,
        work_id: str,
        project_id: str,
        output_kind: str,
        assets: list[dict],
    ) -> list[WorkAssetResponse]:
        with SessionLocal() as session:
            session.execute(delete(WorkAssetModel).where(WorkAssetModel.work_id == work_id))
            models: list[WorkAssetModel] = []
            for index, asset in enumerate(assets):
                model = WorkAssetModel(
                    asset_id=f"asset_{work_id}_{index + 1:03d}",
                    work_id=work_id,
                    project_id=project_id,
                    output_kind=output_kind,
                    asset_type=str(asset.get("asset_type", "TEXT")),
                    order_index=index,
                    title=str(asset.get("title", "")),
                    summary=str(asset.get("summary", "")),
                    url=str(asset.get("url", "")),
                    text_content=str(asset.get("text_content", "")),
                    metadata_json=json.dumps(asset.get("metadata", {}), ensure_ascii=False),
                    updated_at=now_iso(),
                )
                session.add(model)
                models.append(model)
            session.commit()
            return [self._to_asset(item) for item in models]

    def list_assets(self, work_id: str) -> list[WorkAssetResponse]:
        with SessionLocal() as session:
            models = session.execute(
                select(WorkAssetModel)
                .where(WorkAssetModel.work_id == work_id)
                .order_by(WorkAssetModel.order_index.asc()),
            ).scalars().all()
            return [self._to_asset(item) for item in models]

    def list_assets_by_project(self, project_id: str) -> list[WorkAssetResponse]:
        with SessionLocal() as session:
            models = session.execute(
                select(WorkAssetModel)
                .where(WorkAssetModel.project_id == project_id)
                .order_by(WorkAssetModel.order_index.asc()),
            ).scalars().all()
            return [self._to_asset(item) for item in models]

    def delete(self, work_id: str) -> bool:
        with SessionLocal() as session:
            session.execute(delete(WorkAssetModel).where(WorkAssetModel.work_id == work_id))
            result = session.execute(delete(WorkModel).where(WorkModel.work_id == work_id))
            session.commit()
            return result.rowcount > 0

    @staticmethod
    def _to_work(model: WorkModel) -> WorkResponse:
        return WorkResponse(
            workId=model.work_id,
            projectId=model.project_id,
            title=model.title,
            sceneLabel=model.scene_label,
            durationLabel=model.duration_label,
            statusLabel=model.status_label,
            updatedAt=model.updated_at,
            coverUrl=model.cover_url,
            videoUrl=model.video_url,
            outputKind=_output_kind_from_work(
                scene_label=model.scene_label,
                duration_label=model.duration_label,
                video_url=model.video_url,
            ),
        )

    @staticmethod
    def _to_asset(model: WorkAssetModel) -> WorkAssetResponse:
        try:
            metadata = json.loads(model.metadata_json or "{}")
        except json.JSONDecodeError:
            metadata = {}
        return WorkAssetResponse(
            assetId=model.asset_id,
            workId=model.work_id,
            projectId=model.project_id,
            outputKind=model.output_kind,
            assetType=model.asset_type,
            orderIndex=model.order_index,
            title=model.title,
            summary=model.summary,
            url=model.url,
            textContent=model.text_content,
            metadata=metadata if isinstance(metadata, dict) else {},
            updatedAt=model.updated_at,
        )
