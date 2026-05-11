import json
from pathlib import Path
from collections.abc import Callable

from sqlalchemy import create_engine, text
from sqlalchemy.engine import Engine
from sqlalchemy.exc import OperationalError

from app.core.settings import settings
from app.core.time import now_iso
from app.db.base import Base
from app.db.models import (
    MemberPlanModel,
    OrderModel,
    PreviewAssetModel,
    ProjectModel,
    SceneModel,
    StoryDraftModel,
    StoryboardSectionModel,
    TaskModel,
    UserAccountModel,
    WorkModel,
)
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


def _ensure_project_columns() -> None:
    if settings.db_driver.lower() != "mysql":
        return
    with engine.connect() as conn:
        columns = {row[0] for row in conn.execute(text("SHOW COLUMNS FROM projects"))}
        if "theme_line" not in columns:
            conn.execute(text("ALTER TABLE projects ADD COLUMN theme_line VARCHAR(255) NULL"))
        if "selected_style_id" not in columns:
            conn.execute(text("ALTER TABLE projects ADD COLUMN selected_style_id VARCHAR(64) NULL"))
        conn.commit()


def _ensure_task_columns() -> None:
    if settings.db_driver.lower() != "mysql":
        return
    with engine.connect() as conn:
        columns = {row[0] for row in conn.execute(text("SHOW COLUMNS FROM tasks"))}
        if "project_id" not in columns:
            conn.execute(text("ALTER TABLE tasks ADD COLUMN project_id VARCHAR(64) NULL"))
        if "task_type" not in columns:
            conn.execute(text("ALTER TABLE tasks ADD COLUMN task_type VARCHAR(64) NOT NULL DEFAULT 'GENERAL'"))
        conn.commit()


def _ensure_upload_columns() -> None:
    if settings.db_driver.lower() != "mysql":
        return
    with engine.connect() as conn:
        columns = {row[0] for row in conn.execute(text("SHOW COLUMNS FROM uploads"))}
        if "local_path" not in columns:
            conn.execute(text("ALTER TABLE uploads ADD COLUMN local_path VARCHAR(255) NULL"))
        if "vod_vid" not in columns:
            conn.execute(text("ALTER TABLE uploads ADD COLUMN vod_vid VARCHAR(128) NULL"))
        if "vod_file_name" not in columns:
            conn.execute(text("ALTER TABLE uploads ADD COLUMN vod_file_name VARCHAR(255) NULL"))
        conn.commit()


def _seed_if_needed() -> None:
    Path(settings.storage_root).mkdir(parents=True, exist_ok=True)
    with SessionLocal() as session:
        if not session.query(SceneModel).first():
            session.add_all(
                [
                    SceneModel(
                        scene_id="scene_travel",
                        title="旅行纪念",
                        subtitle="把风景、人物和那一天的心情剪成短片",
                        recommended_duration_label="30-60 秒",
                        estimated_time_label="约 2 分钟",
                        cover_url="",
                        supported_material_types_json=json.dumps(["PHOTO", "VIDEO", "AUDIO", "NOTE"], ensure_ascii=False),
                    ),
                    SceneModel(
                        scene_id="scene_memory",
                        title="人生回忆",
                        subtitle="适合老照片、录音和家庭故事整理",
                        recommended_duration_label="60-180 秒",
                        estimated_time_label="约 4 分钟",
                        cover_url="",
                        supported_material_types_json=json.dumps(["PHOTO", "VIDEO", "AUDIO", "NOTE"], ensure_ascii=False),
                    ),
                    SceneModel(
                        scene_id="scene_festival",
                        title="节庆祝福",
                        subtitle="生日、毕业、纪念日都能快速出片",
                        recommended_duration_label="30-45 秒",
                        estimated_time_label="约 2 分钟",
                        cover_url="",
                        supported_material_types_json=json.dumps(["PHOTO", "VIDEO", "AUDIO", "NOTE"], ensure_ascii=False),
                    ),
                    SceneModel(
                        scene_id="scene_hero",
                        title="主角故事",
                        subtitle="更强调人物表达与情绪推进",
                        recommended_duration_label="45-90 秒",
                        estimated_time_label="约 3 分钟",
                        cover_url="",
                        supported_material_types_json=json.dumps(["PHOTO", "VIDEO", "AUDIO", "NOTE"], ensure_ascii=False),
                    ),
                ],
            )

        if not session.query(MemberPlanModel).first():
            session.add_all(
                [
                    MemberPlanModel(
                        plan_id="lite",
                        name="Lite",
                        monthly_price=49,
                        badge=None,
                        summary="适合轻量使用与日常记录整理",
                        features_json=json.dumps(
                            ["1x 基础生成额度", "适合小型项目与轻量创作", "基础故事草稿与预览能力", "1080P 标准导出"],
                            ensure_ascii=False,
                        ),
                    ),
                    MemberPlanModel(
                        plan_id="pro",
                        name="Pro",
                        monthly_price=149,
                        badge="最受欢迎",
                        summary="适合频繁创作与更完整叙事",
                        features_json=json.dumps(
                            ["5x Lite 用量额度", "优先体验新功能与模型", "更多精修能力与高级配乐", "更快生成速度"],
                            ensure_ascii=False,
                        ),
                    ),
                    MemberPlanModel(
                        plan_id="max",
                        name="Max",
                        monthly_price=469,
                        badge="量大管饱",
                        summary="适合高频深度创作与团队场景",
                        features_json=json.dumps(
                            ["20x Lite 用量额度", "高阶生成与批量任务能力", "专属资源优先保障", "更多并发与更长时长支持"],
                            ensure_ascii=False,
                        ),
                    ),
                ],
            )

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
                    theme_line="想把那天的晚风和笑声留下来",
                    selected_style_id="style_warm",
                    updated_at=now_iso(),
                ),
            )
        has_task = session.query(TaskModel).first()
        if not has_task:
            session.add(
                TaskModel(
                    task_id="task_story_seed_001",
                    project_id="project_seed_001",
                    task_type="STORYBOARD",
                    status="RUNNING",
                    progress=72,
                    current_stage="GENERATING_STORYBOARD",
                    estimated_remaining_seconds=45,
                    result_json=None,
                    error_json=json.dumps(None, ensure_ascii=False),
                    updated_at=now_iso(),
                ),
            )
        if not session.query(StoryDraftModel).first():
            session.add(
                StoryDraftModel(
                    project_id="project_seed_001",
                    title="旅行纪念 | 第一版故事",
                    opening="从照片和视频里，先把那天最想留下的一刻轻轻拉近。",
                    body="系统会围绕人物、风景和情绪节奏，把素材组织成一条能分享、也值得反复看的短片主线。",
                    closing="最后留下的不只是风景，而是一起在场时最真实的心情。",
                    updated_at=now_iso(),
                ),
            )
        if not session.query(StoryboardSectionModel).first():
            session.add_all(
                [
                    StoryboardSectionModel(
                        section_id="section_seed_001",
                        project_id="project_seed_001",
                        order_index=0,
                        title="开场",
                        summary="用一张最有氛围感的照片和一句轻旁白拉开记忆。",
                        subtitle_line="字幕：把风吹过的那一刻留下来",
                        duration_label="00:10",
                    ),
                    StoryboardSectionModel(
                        section_id="section_seed_002",
                        project_id="project_seed_001",
                        order_index=1,
                        title="高光",
                        summary="把骑行、笑声与晚霞镜头组合成情绪峰值。",
                        subtitle_line="字幕：那些一路向前的画面都在发光",
                        duration_label="00:22",
                    ),
                    StoryboardSectionModel(
                        section_id="section_seed_003",
                        project_id="project_seed_001",
                        order_index=2,
                        title="收束",
                        summary="用一张静止合照和一句收束旁白完成回落。",
                        subtitle_line="字幕：原来最想记住的是一起在场",
                        duration_label="00:16",
                    ),
                ],
            )
        if not session.query(PreviewAssetModel).first():
            session.add(
                PreviewAssetModel(
                    project_id="project_seed_001",
                    title="旅行纪念 | 预览版",
                    subtitle_summary="已生成三段式故事板，可继续换音乐、改字幕、补镜头。",
                    music_label="配乐：温暖胶片",
                    cover_caption="封面建议：晚霞合照 + 手写标题",
                    video_url="",
                    cover_url="",
                    updated_at=now_iso(),
                ),
            )
        if not session.query(WorkModel).first():
            session.add(
                WorkModel(
                    work_id="work_001",
                    project_id="project_seed_001",
                    title="大理五月风",
                    scene_label="旅行纪念",
                    duration_label="00:48",
                    status_label="已完成",
                    updated_at=now_iso(),
                    cover_url="",
                    video_url="",
                ),
            )
        if not session.query(OrderModel).first():
            session.add_all(
                [
                    OrderModel(
                        order_id="order_20260510_001",
                        user_id="user_001",
                        project_id="project_seed_001",
                        title="大理五月风 导出",
                        amount_label="¥39.90",
                        export_spec="1080P 无水印",
                        status_label="已支付",
                        created_at=now_iso(),
                    ),
                    OrderModel(
                        order_id="order_20260510_002",
                        user_id="user_001",
                        project_id=None,
                        title="会员年卡",
                        amount_label="¥168.00",
                        export_spec="会员权益",
                        status_label="已生效",
                        created_at=now_iso(),
                    ),
                ],
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
            _ensure_project_columns()
            _ensure_task_columns()
            _ensure_upload_columns()
            _seed_if_needed()
            return
        except OperationalError as exc:
            if on_mysql_fallback is not None:
                on_mysql_fallback(exc)
            raise

    Base.metadata.create_all(bind=engine)
    _seed_if_needed()
