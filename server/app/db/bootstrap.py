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
    AuthRefreshTokenModel,
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
    WorkAssetModel,
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
        conn.commit()


def _ensure_url_text_columns() -> None:
    if settings.db_driver.lower() != "mysql":
        return
    with engine.connect() as conn:
        for table_name in ("preview_assets", "works"):
            columns = {
                row[0]: row[1].lower()
                for row in conn.execute(text(f"SHOW COLUMNS FROM {table_name}"))
            }
            for column_name in ("cover_url", "video_url"):
                if column_name in columns and columns[column_name] != "text":
                    conn.execute(text(f"UPDATE {table_name} SET {column_name} = '' WHERE {column_name} IS NULL"))
                    conn.execute(text(f"ALTER TABLE {table_name} MODIFY COLUMN {column_name} TEXT NOT NULL"))
        conn.commit()


def _ensure_work_asset_table() -> None:
    if settings.db_driver.lower() != "mysql":
        return
    with engine.connect() as conn:
        conn.execute(
            text(
                """
                CREATE TABLE IF NOT EXISTS work_assets (
                    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    asset_id VARCHAR(64) NOT NULL,
                    work_id VARCHAR(64) NOT NULL,
                    project_id VARCHAR(64) NOT NULL,
                    output_kind VARCHAR(64) NOT NULL,
                    asset_type VARCHAR(64) NOT NULL,
                    order_index INT NOT NULL,
                    title VARCHAR(255) NOT NULL,
                    summary TEXT NOT NULL,
                    url TEXT NOT NULL,
                    text_content TEXT NOT NULL,
                    metadata_json TEXT NOT NULL,
                    updated_at VARCHAR(64) NOT NULL,
                    UNIQUE KEY ix_work_assets_asset_id (asset_id),
                    KEY ix_work_assets_work_id (work_id),
                    KEY ix_work_assets_project_id (project_id),
                    KEY ix_work_assets_output_kind (output_kind),
                    KEY ix_work_assets_asset_type (asset_type)
                ) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
                """
            ),
        )
        conn.commit()


def _ensure_auth_refresh_token_table() -> None:
    if settings.db_driver.lower() != "mysql":
        return
    with engine.connect() as conn:
        conn.execute(
            text(
                """
                CREATE TABLE IF NOT EXISTS auth_refresh_tokens (
                    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    token_id VARCHAR(64) NOT NULL,
                    user_id VARCHAR(64) NOT NULL,
                    token_hash VARCHAR(128) NOT NULL,
                    device_id VARCHAR(128) NOT NULL DEFAULT '',
                    revoked TINYINT(1) NOT NULL DEFAULT 0,
                    expires_at_epoch BIGINT NOT NULL,
                    created_at VARCHAR(64) NOT NULL,
                    updated_at VARCHAR(64) NOT NULL,
                    UNIQUE KEY ix_auth_refresh_tokens_token_id (token_id),
                    UNIQUE KEY ix_auth_refresh_tokens_token_hash (token_hash),
                    KEY ix_auth_refresh_tokens_user_id (user_id),
                    KEY ix_auth_refresh_tokens_revoked (revoked),
                    KEY ix_auth_refresh_tokens_expires_at_epoch (expires_at_epoch)
                ) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
                """
            ),
        )
        conn.commit()


def _seed_if_needed() -> None:
    Path(settings.storage_root).mkdir(parents=True, exist_ok=True)
    with SessionLocal() as session:
        scene_seed_data = [
            {
                "scene_id": "scene_character_xiyou",
                "title": "西游记角色穿越",
                "subtitle": "上传自拍，把用户架空成唐僧、女儿国行者或取经路上的新角色",
                "recommended_duration_label": "角色海报 + 短篇故事",
                "estimated_time_label": "约 2 分钟",
                "supported_material_types": ["PHOTO", "NOTE"],
            },
            {
                "scene_id": "scene_character_honglou",
                "title": "红楼梦角色穿越",
                "subtitle": "把人物放进贾府关系网，生成贾宝玉式公子或新入园人物",
                "recommended_duration_label": "角色设定 + 剧情片段",
                "estimated_time_label": "约 2 分钟",
                "supported_material_types": ["PHOTO", "NOTE"],
            },
            {
                "scene_id": "scene_outline_history",
                "title": "融合历史小说",
                "subtitle": "把用户大纲融入名著、朝代、宫廷、武侠或架空历史",
                "recommended_duration_label": "短篇小说 + 分镜",
                "estimated_time_label": "约 3 分钟",
                "supported_material_types": ["NOTE"],
            },
            {
                "scene_id": "scene_outline_original",
                "title": "原创故事小说",
                "subtitle": "从一句灵感扩写成专属世界观、人物关系和章节梗概",
                "recommended_duration_label": "短篇小说 + 连载大纲",
                "estimated_time_label": "约 3 分钟",
                "supported_material_types": ["NOTE"],
            },
            {
                "scene_id": "scene_media_comic",
                "title": "生成连环漫画",
                "subtitle": "解析小说人物、场景和情节节点，输出 6-12 格漫画分镜",
                "recommended_duration_label": "连环漫画脚本",
                "estimated_time_label": "约 4 分钟",
                "supported_material_types": ["NOTE"],
            },
            {
                "scene_id": "scene_media_short_video",
                "title": "生成短视频",
                "subtitle": "把小说拆成镜头、旁白、字幕和封面建议，适合推文视频",
                "recommended_duration_label": "30-90 秒短视频",
                "estimated_time_label": "约 4 分钟",
                "supported_material_types": ["NOTE"],
            },
        ]
        for item in scene_seed_data:
            existing_scene = session.query(SceneModel).filter(SceneModel.scene_id == item["scene_id"]).first()
            if existing_scene is None:
                session.add(
                    SceneModel(
                        scene_id=item["scene_id"],
                        title=item["title"],
                        subtitle=item["subtitle"],
                        recommended_duration_label=item["recommended_duration_label"],
                        estimated_time_label=item["estimated_time_label"],
                        cover_url="",
                        supported_material_types_json=json.dumps(item["supported_material_types"], ensure_ascii=False),
                    ),
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
                    scene_id="scene_character_xiyou",
                    scene_title="西游记角色穿越",
                    mode="CHARACTER_TIME_TRAVEL",
                    title="我在女儿国当取经人",
                    status="PREVIEW_READY",
                    progress=100,
                    current_step="角色短剧已生成",
                    material_count=2,
                    theme_line="我想把这张照片里的主角架空成西游记里的取经人，温和但有命运感。",
                    selected_style_id="style_classic",
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
                    title="西游记角色穿越 | 第一版创作草稿",
                    opening="主角一睁眼，已身披素色僧衣，站在女儿国城门外。",
                    body="系统会围绕用户照片气质、取经世界规则和第一场命运冲突，生成角色身份、人物关系和短视频改编主线。",
                    closing="下一步可继续生成角色定妆图、连环漫画格和 30 秒短视频分镜。",
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
                        title="身份亮相",
                        summary="用角色定妆照建立取经人身份，背景是女儿国城门与薄雾晨光。",
                        subtitle_line="字幕：他一睁眼，已站在旧故事的分岔口",
                        duration_label="00:08",
                    ),
                    StoryboardSectionModel(
                        section_id="section_seed_002",
                        project_id="project_seed_001",
                        order_index=1,
                        title="冲突推进",
                        summary="主角与女儿国国王第一次相见，既要守住取经使命，也被迫面对自己新的身份。",
                        subtitle_line="字幕：旧规矩还在，他却先改了第一步",
                        duration_label="00:18",
                    ),
                    StoryboardSectionModel(
                        section_id="section_seed_003",
                        project_id="project_seed_001",
                        order_index=2,
                        title="悬念收束",
                        summary="夜色降临，通关文牒上出现陌生姓名，暗示下一集命运改写。",
                        subtitle_line="字幕：下一回，真正的考验才开始",
                        duration_label="00:10",
                    ),
                ],
            )
        if not session.query(PreviewAssetModel).first():
            session.add(
                PreviewAssetModel(
                    project_id="project_seed_001",
                    title="西游记角色穿越 | 预览版",
                    subtitle_summary="已生成角色设定和三段式分镜，可继续生成角色海报、漫画格或短视频。",
                    music_label="配乐：古风悬念",
                    cover_caption="封面建议：角色定妆照 + 女儿国城门",
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
                    title="我在女儿国当取经人",
                    scene_label="西游记角色穿越",
                    duration_label="角色海报 + 00:38",
                    status_label="已完成",
                    updated_at=now_iso(),
                    cover_url="",
                    video_url="",
                ),
            )
        if not session.query(WorkAssetModel).first():
            session.add_all(
                [
                    WorkAssetModel(
                        asset_id="asset_seed_001",
                        work_id="work_001",
                        project_id="project_seed_001",
                        output_kind="CHARACTER_STORY",
                        asset_type="TEXT",
                        order_index=0,
                        title="角色故事正文",
                        summary="西游记角色穿越的成品故事包。",
                        url="",
                        text_content="主角一睁眼，已身披素色僧衣，站在女儿国城门外。通关文牒上多出的陌生姓名，意味着他不只是旁观者，而是这段取经路的新变量。",
                        metadata_json=json.dumps({"label": "角色故事包"}, ensure_ascii=False),
                        updated_at=now_iso(),
                    ),
                ],
            )
        if not session.query(OrderModel).first():
            session.add_all(
                [
                    OrderModel(
                        order_id="order_20260510_001",
                        user_id="user_001",
                        project_id="project_seed_001",
                        title="我在女儿国当取经人 导出",
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
            _ensure_url_text_columns()
            _ensure_work_asset_table()
            _ensure_auth_refresh_token_table()
            _seed_if_needed()
            return
        except OperationalError as exc:
            if on_mysql_fallback is not None:
                on_mysql_fallback(exc)
            raise

    Base.metadata.create_all(bind=engine)
    _seed_if_needed()
