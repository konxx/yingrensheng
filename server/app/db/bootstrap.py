import json
from collections.abc import Callable

from sqlalchemy import create_engine, text
from sqlalchemy.engine import Engine
from sqlalchemy.exc import OperationalError

from app.core.settings import settings
from app.core.time import now_iso
from app.db.base import Base
from app.db.models import ProjectModel, TaskModel
from app.db.session import SessionLocal, engine


def _try_create_mysql_database() -> None:
    admin_url = (
        f"mysql+pymysql://{settings.mysql_user}:{settings.mysql_password}"
        f"@{settings.mysql_host}:{settings.mysql_port}/?charset=utf8mb4"
    )
    admin_engine: Engine | None = None
    try:
        admin_engine = create_engine(admin_url, future=True, pool_pre_ping=True)
        with admin_engine.connect() as conn:
            conn.execute(
                text(
                    f"CREATE DATABASE IF NOT EXISTS `{settings.mysql_database}` "
                    "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
                ),
            )
            conn.commit()
    finally:
        if admin_engine is not None:
            admin_engine.dispose()


def _seed_if_needed() -> None:
    with SessionLocal() as session:
        has_project = session.query(ProjectModel).first()
        if not has_project:
            session.add(
                ProjectModel(
                    project_id="project_seed_001",
                    scene_id="scene_travel",
                    scene_title="旅行纪念",
                    mode="QUICK_FILM",
                    title="大理五月风",
                    status="PREVIEW_READY",
                    progress=100,
                    current_step="首版已生成",
                    material_count=24,
                    updated_at=now_iso(),
                ),
            )
        has_task = session.query(TaskModel).first()
        if not has_task:
            session.add(
                TaskModel(
                    task_id="task_story_seed_001",
                    status="RUNNING",
                    progress=72,
                    current_stage="GENERATING_STORYBOARD",
                    estimated_remaining_seconds=45,
                    result_json=None,
                    error_json=json.dumps(None, ensure_ascii=False),
                    updated_at=now_iso(),
                ),
            )
        session.commit()


def initialize_database(on_mysql_fallback: Callable[[Exception], None] | None = None) -> None:
    if settings.db_driver.lower() == "mysql":
        try:
            _try_create_mysql_database()
            Base.metadata.create_all(bind=engine)
            _seed_if_needed()
            return
        except OperationalError as exc:
            if on_mysql_fallback is not None:
                on_mysql_fallback(exc)
            raise

    Base.metadata.create_all(bind=engine)
    _seed_if_needed()
