import json
from collections.abc import Callable

from sqlalchemy import create_engine, text
from sqlalchemy.engine import Engine
from sqlalchemy.exc import OperationalError

from app.core.settings import settings
from app.core.time import now_iso
from app.db.base import Base
from app.db.models import ProjectModel, TaskModel, UserAccountModel
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


def _ensure_user_accounts_columns() -> None:
    if settings.db_driver.lower() != "mysql":
        return
    with engine.connect() as conn:
        result = conn.execute(text("SHOW COLUMNS FROM user_accounts"))
        columns = {row[0] for row in result}

        if "phone" in columns and "username" not in columns:
            conn.execute(text("ALTER TABLE user_accounts CHANGE COLUMN phone username VARCHAR(64) NOT NULL"))
        if "email" not in columns:
            conn.execute(text("ALTER TABLE user_accounts ADD COLUMN email VARCHAR(128) NOT NULL DEFAULT ''"))
        conn.execute(
            text(
                "UPDATE user_accounts "
                "SET email = CONCAT(username, '@yingrensheng.local') "
                "WHERE email = '' OR email IS NULL"
            ),
        )

        index_rows = conn.execute(text("SHOW INDEX FROM user_accounts")).fetchall()
        index_names = {row[2] for row in index_rows}
        if "ix_user_accounts_username" not in index_names:
            conn.execute(text("CREATE UNIQUE INDEX ix_user_accounts_username ON user_accounts (username)"))
        if "ix_user_accounts_email" not in index_names:
            conn.execute(text("CREATE UNIQUE INDEX ix_user_accounts_email ON user_accounts (email)"))
        conn.commit()


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
        admin_account = session.query(UserAccountModel).filter(UserAccountModel.user_id == "admin_001").first()
        if admin_account is None:
            session.add(
                UserAccountModel(
                    user_id="admin_001",
                    username="admin",
                    email="admin@yingrensheng.local",
                    nickname="Administrator",
                    password="admin",
                    avatar_url="",
                    role="admin",
                ),
            )
        else:
            admin_account.username = "admin"
            admin_account.email = admin_account.email or "admin@yingrensheng.local"
            admin_account.nickname = "Administrator"
            admin_account.role = "admin"

        demo_user = session.query(UserAccountModel).filter(UserAccountModel.user_id == "user_001").first()
        if demo_user is None:
            session.add(
                UserAccountModel(
                    user_id="user_001",
                    username="demo",
                    email="demo@yingrensheng.local",
                    nickname="林青",
                    password="123456",
                    avatar_url="",
                    role="user",
                ),
            )
        else:
            demo_user.username = demo_user.username or "demo"
            demo_user.email = demo_user.email or "demo@yingrensheng.local"
            demo_user.nickname = demo_user.nickname or "林青"
            demo_user.role = "user"
        session.commit()


def initialize_database(on_mysql_fallback: Callable[[Exception], None] | None = None) -> None:
    if settings.db_driver.lower() == "mysql":
        try:
            _try_create_mysql_database()
            Base.metadata.create_all(bind=engine)
            _ensure_user_accounts_columns()
            _seed_if_needed()
            return
        except OperationalError as exc:
            if on_mysql_fallback is not None:
                on_mysql_fallback(exc)
            raise

    Base.metadata.create_all(bind=engine)
    _seed_if_needed()
