from sqlalchemy import BigInteger, Boolean, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base


class SceneModel(Base):
    __tablename__ = "scenes"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    scene_id: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    title: Mapped[str] = mapped_column(String(128))
    subtitle: Mapped[str] = mapped_column(String(255))
    recommended_duration_label: Mapped[str] = mapped_column(String(64))
    estimated_time_label: Mapped[str] = mapped_column(String(64))
    cover_url: Mapped[str] = mapped_column(String(255), default="")
    supported_material_types_json: Mapped[str] = mapped_column(Text)


class MemberPlanModel(Base):
    __tablename__ = "member_plans"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    plan_id: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    name: Mapped[str] = mapped_column(String(32))
    monthly_price: Mapped[int] = mapped_column(Integer)
    badge: Mapped[str | None] = mapped_column(String(64), nullable=True)
    summary: Mapped[str] = mapped_column(String(255))
    features_json: Mapped[str] = mapped_column(Text)


class ProjectModel(Base):
    __tablename__ = "projects"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    project_id: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    scene_id: Mapped[str] = mapped_column(String(64), index=True)
    scene_title: Mapped[str] = mapped_column(String(128))
    mode: Mapped[str] = mapped_column(String(32))
    title: Mapped[str] = mapped_column(String(255))
    status: Mapped[str] = mapped_column(String(32), index=True)
    progress: Mapped[int] = mapped_column(Integer, default=0)
    current_step: Mapped[str] = mapped_column(String(255))
    material_count: Mapped[int] = mapped_column(Integer, default=0)
    theme_line: Mapped[str | None] = mapped_column(String(255), nullable=True)
    selected_style_id: Mapped[str | None] = mapped_column(String(64), nullable=True)
    updated_at: Mapped[str] = mapped_column(String(64))


class UploadModel(Base):
    __tablename__ = "uploads"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    upload_id: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    project_id: Mapped[str] = mapped_column(String(64), index=True)
    file_name: Mapped[str] = mapped_column(String(255))
    mime_type: Mapped[str] = mapped_column(String(128))
    size_bytes: Mapped[int] = mapped_column(BigInteger)
    sha256: Mapped[str] = mapped_column(String(128))
    status: Mapped[str] = mapped_column(String(32))
    uploaded_part_count: Mapped[int] = mapped_column(Integer, default=0)
    total_part_count: Mapped[int] = mapped_column(Integer, default=1)
    deduplicated: Mapped[bool] = mapped_column(Boolean, default=False)
    material_id: Mapped[str | None] = mapped_column(String(64), nullable=True)
    local_path: Mapped[str | None] = mapped_column(String(255), nullable=True)


class MaterialModel(Base):
    __tablename__ = "materials"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    material_id: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    project_id: Mapped[str] = mapped_column(String(64), index=True)
    title: Mapped[str] = mapped_column(String(255))
    material_type: Mapped[str] = mapped_column(String(32), index=True)
    duration_label: Mapped[str] = mapped_column(String(64))
    insight: Mapped[str] = mapped_column(Text)
    local_path: Mapped[str] = mapped_column(String(255))
    file_name: Mapped[str] = mapped_column(String(255))
    mime_type: Mapped[str] = mapped_column(String(128))
    size_bytes: Mapped[int] = mapped_column(BigInteger)
    sha256: Mapped[str] = mapped_column(String(128), index=True)
    status: Mapped[str] = mapped_column(String(32), index=True)
    created_at: Mapped[str] = mapped_column(String(64))
    updated_at: Mapped[str] = mapped_column(String(64))


class TaskModel(Base):
    __tablename__ = "tasks"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    task_id: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    status: Mapped[str] = mapped_column(String(32), index=True)
    project_id: Mapped[str | None] = mapped_column(String(64), nullable=True, index=True)
    task_type: Mapped[str] = mapped_column(String(64), default="GENERAL", index=True)
    progress: Mapped[int] = mapped_column(Integer, default=0)
    current_stage: Mapped[str] = mapped_column(String(64))
    estimated_remaining_seconds: Mapped[int] = mapped_column(Integer, default=0)
    result_json: Mapped[str | None] = mapped_column(Text, nullable=True)
    error_json: Mapped[str | None] = mapped_column(Text, nullable=True)
    updated_at: Mapped[str] = mapped_column(String(64))


class StoryDraftModel(Base):
    __tablename__ = "story_drafts"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    project_id: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    title: Mapped[str] = mapped_column(String(255))
    opening: Mapped[str] = mapped_column(Text)
    body: Mapped[str] = mapped_column(Text)
    closing: Mapped[str] = mapped_column(Text)
    updated_at: Mapped[str] = mapped_column(String(64))


class StoryboardSectionModel(Base):
    __tablename__ = "storyboard_sections"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    section_id: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    project_id: Mapped[str] = mapped_column(String(64), index=True)
    order_index: Mapped[int] = mapped_column(Integer)
    title: Mapped[str] = mapped_column(String(128))
    summary: Mapped[str] = mapped_column(Text)
    subtitle_line: Mapped[str] = mapped_column(Text)
    duration_label: Mapped[str] = mapped_column(String(64))


class PreviewAssetModel(Base):
    __tablename__ = "preview_assets"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    project_id: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    title: Mapped[str] = mapped_column(String(255))
    subtitle_summary: Mapped[str] = mapped_column(Text)
    music_label: Mapped[str] = mapped_column(String(128))
    cover_caption: Mapped[str] = mapped_column(String(255))
    video_url: Mapped[str] = mapped_column(String(255), default="")
    cover_url: Mapped[str] = mapped_column(String(255), default="")
    updated_at: Mapped[str] = mapped_column(String(64))


class WorkModel(Base):
    __tablename__ = "works"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    work_id: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    project_id: Mapped[str] = mapped_column(String(64), index=True)
    title: Mapped[str] = mapped_column(String(255))
    scene_label: Mapped[str] = mapped_column(String(128))
    duration_label: Mapped[str] = mapped_column(String(64))
    status_label: Mapped[str] = mapped_column(String(64))
    updated_at: Mapped[str] = mapped_column(String(64))
    cover_url: Mapped[str] = mapped_column(String(255), default="")
    video_url: Mapped[str] = mapped_column(String(255), default="")


class OrderModel(Base):
    __tablename__ = "orders"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    order_id: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    user_id: Mapped[str] = mapped_column(String(64), index=True)
    project_id: Mapped[str | None] = mapped_column(String(64), nullable=True, index=True)
    title: Mapped[str] = mapped_column(String(255))
    amount_label: Mapped[str] = mapped_column(String(64))
    export_spec: Mapped[str] = mapped_column(String(128))
    status_label: Mapped[str] = mapped_column(String(64))
    created_at: Mapped[str] = mapped_column(String(64))


class UserAccountModel(Base):
    __tablename__ = "user_accounts"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    user_id: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    username: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    email: Mapped[str] = mapped_column(String(128), unique=True, index=True)
    nickname: Mapped[str] = mapped_column(String(128))
    password: Mapped[str] = mapped_column(String(255))
    avatar_url: Mapped[str] = mapped_column(String(255), default="")
    role: Mapped[str] = mapped_column(String(32), default="user", index=True)
