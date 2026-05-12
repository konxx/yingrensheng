from dataclasses import dataclass, field
import json
import uuid

from app.core.settings import settings
from app.repositories.project_repository import ProjectRepository
from app.repositories.creation_repository import CreationRepository
from app.repositories.member_repository import MemberRepository
from app.repositories.material_repository import MaterialRepository
from app.repositories.order_repository import OrderRepository
from app.repositories.scene_repository import SceneRepository
from app.repositories.task_repository import TaskRepository
from app.repositories.upload_repository import UploadRepository
from app.repositories.user_repository import UserRepository
from app.repositories.work_repository import WorkRepository
from app.schemas.creation import (
    InterviewPromptResponse,
    PreviewAssetResponse,
    StoryDraftGenerateRequest,
    StoryDraftResponse,
    StoryboardSectionResponse,
)
from app.schemas.auth import LoginResponseData, RegisterRequest
from app.schemas.common import PageResponse
from app.schemas.export import ExportGenerateRequest
from app.schemas.member import MemberPlanResponse
from app.schemas.order import OrderResponse
from app.schemas.project import CreateProjectRequest, ProjectDetail
from app.schemas.scene import SceneListItem
from app.schemas.task import TaskStatusResponse
from app.schemas.upload import UploadInitiateRequest, UploadInitiateResponse, UploadStatusResponse
from app.schemas.user import MemberMeResponse, UpdateUserProfileRequest, UserProfile
from app.schemas.work import WorkResponse
from app.services.dashscope_text_client import DashScopeTextClient
from app.services.material_service import MaterialService
from app.services.render_service import RenderService


@dataclass
class AuthService:
    repository: UserRepository

    def send_sms(self, username: str, purpose: str) -> None:
        _ = (username, purpose)

    def login(self, username: str, sms_code: str, device_id: str, password: str | None = None) -> LoginResponseData:
        _ = (sms_code, device_id)
        profile = self.repository.verify_user(username=username, password=password or "123456")
        if profile is None:
            raise ValueError("INVALID_CREDENTIALS")
        return LoginResponseData(
            accessToken="token_dev_001",
            refreshToken="refresh_dev_001",
            expiresInSeconds=7200,
            user=profile,
        )

    def register(self, payload: RegisterRequest) -> LoginResponseData:
        existing = self.repository.get_by_username(payload.username)
        if existing is not None:
            raise ValueError("USERNAME_ALREADY_REGISTERED")
        profile = self.repository.create_user(
            username=payload.username,
            nickname=payload.nickname,
            password=payload.password,
            email=payload.email,
        )
        return LoginResponseData(
            accessToken="token_dev_register",
            refreshToken="refresh_dev_register",
            expiresInSeconds=7200,
            user=profile,
        )


@dataclass
class UserService:
    repository: UserRepository

    def get_current_user(self) -> UserProfile:
        profile = self.repository.verify_user(username="demo", password="123456")
        if profile is not None:
            return profile
        return UserProfile(
            userId="user_001",
            username="demo",
            email="demo@yingrensheng.local",
            nickname="林青",
            avatarUrl="",
        )

    def get_member_info(self) -> MemberMeResponse:
        return MemberMeResponse(
            levelName="Lite",
            subtitle="当前开发版默认账号为 Lite 会员，后续会随订阅开通升级到 Pro / Max。",
            remainingExportCount=3,
            activePlanPriceLabel="连续包月 ¥49",
        )

    def update_profile(self, payload: UpdateUserProfileRequest) -> UserProfile:
        profile = self.repository.update_profile(
            user_id=payload.user_id,
            username=payload.username,
            nickname=payload.nickname,
            email=payload.email,
        )
        if profile is None:
            raise ValueError("USER_NOT_FOUND")
        return profile


@dataclass
class SceneService:
    repository: SceneRepository

    def list_scenes(self) -> list[SceneListItem]:
        return self.repository.list()

    def scene_title_map(self) -> dict[str, str]:
        return {
            "scene_character_xiyou": "西游记角色穿越",
            "scene_character_honglou": "红楼梦角色穿越",
            "scene_outline_history": "融合历史小说",
            "scene_outline_original": "原创故事小说",
            "scene_media_comic": "生成连环漫画",
            "scene_media_short_video": "生成短视频",
        }


@dataclass
class MemberService:
    repository: MemberRepository

    def list_plans(self) -> list[MemberPlanResponse]:
        return self.repository.list_plans()


@dataclass
class ProjectService:
    repository: ProjectRepository
    scene_service: SceneService

    def create_project(self, payload: CreateProjectRequest) -> ProjectDetail:
        scene_title = self.scene_service.scene_title_map().get(payload.scene_id, payload.scene_id)
        return self.repository.create(payload=payload, scene_title=scene_title)

    def list_projects(self, status: str | None, page: int, page_size: int) -> PageResponse[ProjectDetail]:
        return self.repository.list(status=status, page=page, page_size=page_size)

    def get_project(self, project_id: str) -> ProjectDetail | None:
        return self.repository.get(project_id)


@dataclass
class UploadService:
    repository: UploadRepository

    def initiate_upload(self, payload: UploadInitiateRequest) -> UploadInitiateResponse:
        return self.repository.create(payload)

    def get_upload(self, upload_id: str) -> UploadStatusResponse | None:
        return self.repository.get(upload_id)


@dataclass
class TaskService:
    repository: TaskRepository

    def get_task(self, task_id: str) -> TaskStatusResponse | None:
        return self.repository.get(task_id)


@dataclass
class WorkService:
    repository: WorkRepository

    def list_works(self) -> list[WorkResponse]:
        return self.repository.list()


@dataclass
class OrderService:
    repository: OrderRepository

    def list_orders(self) -> list[OrderResponse]:
        return self.repository.list(user_id="user_001")

    def create_order(
        self,
        user_id: str,
        project_id: str | None,
        title: str,
        amount_label: str,
        export_spec: str,
        status_label: str,
    ) -> OrderResponse:
        return self.repository.create(
            user_id=user_id,
            project_id=project_id,
            title=title,
            amount_label=amount_label,
            export_spec=export_spec,
            status_label=status_label,
        )


@dataclass
class CreationService:
    project_repository: ProjectRepository
    creation_repository: CreationRepository
    task_repository: TaskRepository
    work_repository: WorkRepository
    order_repository: OrderRepository
    text_client: DashScopeTextClient
    render_service: RenderService

    def _create_story_draft(
        self,
        project: ProjectDetail,
        style_id: str,
        theme_line: str,
    ) -> StoryDraftResponse:
        draft = self.text_client.generate_story_draft(
            scene_title=project.scene_title,
            project_title=project.title,
            theme_line=theme_line,
            style_id=style_id,
        )
        return self.creation_repository.upsert_story_draft(
            project_id=project.project_id,
            title=draft["title"],
            opening=draft["opening"],
            body=draft["body"],
            closing=draft["closing"],
        )

    def get_interview_prompts(self, scene_id: str | None = None) -> list[InterviewPromptResponse]:
        if scene_id and scene_id.startswith("scene_character"):
            return [
                InterviewPromptResponse(promptId="prompt_1", title="希望主角更像原著人物，还是一个新角色？", helper="例如唐僧转世、贾府贵公子、女儿国新来客。"),
                InterviewPromptResponse(promptId="prompt_2", title="要保留用户照片里的哪些气质？", helper="比如清冷、少年感、温和、英气、贵气。"),
                InterviewPromptResponse(promptId="prompt_3", title="希望整体更古典、热血、悬疑还是温柔？", helper="这会影响文风、画风、旁白和字幕。"),
            ]
        if scene_id and scene_id.startswith("scene_outline"):
            return [
                InterviewPromptResponse(promptId="prompt_1", title="故事最核心的冲突是什么？", helper="一句话说清主角想要什么、阻力来自哪里。"),
                InterviewPromptResponse(promptId="prompt_2", title="想要哪种时代或题材质感？", helper="例如唐宋、明清、民国、武侠、宫廷、修仙。"),
                InterviewPromptResponse(promptId="prompt_3", title="希望整体更古典、热血、悬疑还是温柔？", helper="这会影响文风、画风、旁白和字幕。"),
            ]
        if scene_id and scene_id.startswith("scene_media"):
            return [
                InterviewPromptResponse(promptId="prompt_1", title="这次优先做漫画还是短视频？", helper="如果已在上一页选择模板，也可以补充希望的节奏。"),
                InterviewPromptResponse(promptId="prompt_2", title="最想突出哪一个剧情节点？", helper="适合作为漫画第 1 格或短视频前 5 秒钩子。"),
                InterviewPromptResponse(promptId="prompt_3", title="希望整体更古典、热血、悬疑还是温柔？", helper="这会影响文风、画风、旁白和字幕。"),
            ]
        return [
            InterviewPromptResponse(promptId="prompt_1", title="这次最想表达什么？", helper="一句话写清创作目标。"),
            InterviewPromptResponse(promptId="prompt_2", title="希望 AI 优先抓住哪段内容？", helper="例如人物、关系、转折或情绪。"),
            InterviewPromptResponse(promptId="prompt_3", title="希望整体更古典、热血、悬疑还是温柔？", helper="这会影响文风、画风、旁白和字幕。"),
        ]

    def generate_story_draft(self, project_id: str, style_id: str, theme_line: str) -> TaskStatusResponse:
        project = self.project_repository.get(project_id)
        if project is None:
            raise ValueError("PROJECT_NOT_FOUND")
        self._create_story_draft(
            project=project,
            style_id=style_id,
            theme_line=theme_line,
        )
        return self.task_repository.upsert(
            task_id=f"task_story_{uuid.uuid4().hex[:8]}",
            project_id=project_id,
            task_type="STORY_DRAFT",
            status="SUCCEEDED",
            progress=100,
            current_stage="GENERATING_STORY_DRAFT",
            estimated_remaining_seconds=0,
            result_json=json.dumps({"projectId": project_id}, ensure_ascii=False),
        )

    def get_story_draft(self, project_id: str) -> StoryDraftResponse | None:
        return self.creation_repository.get_story_draft(project_id)

    def generate_storyboard(self, project_id: str) -> TaskStatusResponse:
        project = self.project_repository.get(project_id)
        if project is None:
            raise ValueError("PROJECT_NOT_FOUND")
        draft = self.creation_repository.get_story_draft(project_id)
        if draft is None:
            draft = self._create_story_draft(
                project=project,
                style_id="style_cinematic",
                theme_line="一个现代人进入古典小说世界，改写自己和主角的命运。",
            )
        sections = self.text_client.generate_storyboard(
            scene_title=project.scene_title,
            story_draft={
                "title": draft.title,
                "opening": draft.opening,
                "body": draft.body,
                "closing": draft.closing,
            },
        )
        self.creation_repository.replace_storyboard(project_id=project_id, sections=sections)
        return self.task_repository.upsert(
            task_id=f"task_board_{uuid.uuid4().hex[:8]}",
            project_id=project_id,
            task_type="STORYBOARD",
            status="SUCCEEDED",
            progress=100,
            current_stage="GENERATING_STORYBOARD",
            estimated_remaining_seconds=0,
            result_json=json.dumps({"projectId": project_id}, ensure_ascii=False),
        )

    def get_storyboard(self, project_id: str) -> list[StoryboardSectionResponse]:
        return self.creation_repository.get_storyboard(project_id)

    def generate_preview(self, project_id: str) -> TaskStatusResponse:
        project = self.project_repository.get(project_id)
        draft = self.creation_repository.get_story_draft(project_id)
        sections = self.creation_repository.get_storyboard(project_id)
        if project is None or draft is None or not sections:
            raise ValueError("PREVIEW_PREREQUISITES_MISSING")
        try:
            video_url, cover_url = self.render_service.create_preview_video(
                project_id=project_id,
                title=draft.title,
            )
            preview = self.creation_repository.upsert_preview(
                project_id=project_id,
                title=draft.title,
                subtitle_summary="已根据故事草稿与三段式故事板生成开发版预览。",
                music_label="配乐：开发版默认配乐",
                cover_caption="封面建议：系统生成",
                video_url=video_url,
                cover_url=cover_url,
            )
            return self.task_repository.upsert(
                task_id=f"task_preview_{uuid.uuid4().hex[:8]}",
                project_id=project_id,
                task_type="PREVIEW",
                status="SUCCEEDED",
                progress=100,
                current_stage="RENDERING_PREVIEW",
                estimated_remaining_seconds=0,
                result_json=json.dumps({"videoUrl": preview.video_url}, ensure_ascii=False),
            )
        except FileNotFoundError as exc:
            return self.task_repository.upsert(
                task_id=f"task_preview_{uuid.uuid4().hex[:8]}",
                project_id=project_id,
                task_type="PREVIEW",
                status="FAILED",
                progress=0,
                current_stage="RENDERING_PREVIEW",
                estimated_remaining_seconds=0,
                error_json=json.dumps({"message": str(exc)}, ensure_ascii=False),
            )

    def get_preview(self, project_id: str) -> PreviewAssetResponse | None:
        return self.creation_repository.get_preview(project_id)

    def export_project(self, project_id: str, payload: ExportGenerateRequest) -> TaskStatusResponse:
        project = self.project_repository.get(project_id)
        preview = self.creation_repository.get_preview(project_id)
        if project is None or preview is None:
            raise ValueError("EXPORT_PREREQUISITES_MISSING")

        self.order_repository.create(
            user_id="user_001",
            project_id=project_id,
            title=f"{project.title} 导出",
            amount_label="¥39.90" if payload.export_plan_id == "plan_single" else "¥168.00",
            export_spec=f"{payload.resolution} {'无水印' if payload.remove_watermark else '带水印'}",
            status_label="已支付",
        )
        self.work_repository.create_or_update(
            project_id=project_id,
            title=project.title,
            scene_label=project.scene_title,
            duration_label="00:48",
            status_label="已完成",
            cover_url=preview.cover_url,
            video_url=preview.video_url,
        )
        return self.task_repository.upsert(
            task_id=f"task_export_{uuid.uuid4().hex[:8]}",
            project_id=project_id,
            task_type="EXPORT",
            status="SUCCEEDED",
            progress=100,
            current_stage="RENDERING_EXPORT",
            estimated_remaining_seconds=0,
            result_json=json.dumps({"projectId": project_id, "videoUrl": preview.video_url}, ensure_ascii=False),
        )


@dataclass
class ServiceContainer:
    user_repository: UserRepository = field(default_factory=UserRepository)
    scene_repository: SceneRepository = field(default_factory=SceneRepository)
    member_repository: MemberRepository = field(default_factory=MemberRepository)
    project_repository: ProjectRepository = field(default_factory=ProjectRepository)
    task_repository: TaskRepository = field(default_factory=TaskRepository)
    creation_repository: CreationRepository = field(default_factory=CreationRepository)
    material_repository: MaterialRepository = field(default_factory=MaterialRepository)
    work_repository: WorkRepository = field(default_factory=WorkRepository)
    order_repository: OrderRepository = field(default_factory=OrderRepository)
    text_client: DashScopeTextClient = field(default_factory=DashScopeTextClient)
    render_service: RenderService = field(default_factory=RenderService)
    scene_service: SceneService = field(init=False)
    member_service: MemberService = field(init=False)
    auth_service: AuthService = field(init=False)
    user_service: UserService = field(init=False)
    project_service: ProjectService = field(init=False)
    upload_service: UploadService = field(init=False)
    material_service: MaterialService = field(init=False)
    task_service: TaskService = field(init=False)
    creation_service: CreationService = field(init=False)
    order_service: OrderService = field(init=False)
    work_service: WorkService = field(init=False)

    def __post_init__(self) -> None:
        self.scene_service = SceneService(repository=self.scene_repository)
        self.member_service = MemberService(repository=self.member_repository)
        self.auth_service = AuthService(repository=self.user_repository)
        self.user_service = UserService(repository=self.user_repository)
        self.project_service = ProjectService(
            repository=self.project_repository,
            scene_service=self.scene_service,
        )
        self.upload_service = UploadService(repository=UploadRepository())
        self.material_service = MaterialService(
            material_repository=self.material_repository,
            upload_repository=UploadRepository(),
        )
        self.task_service = TaskService(repository=self.task_repository)
        self.creation_service = CreationService(
            project_repository=self.project_repository,
            creation_repository=self.creation_repository,
            task_repository=self.task_repository,
            work_repository=self.work_repository,
            order_repository=self.order_repository,
            text_client=self.text_client,
            render_service=self.render_service,
        )
        self.work_service = WorkService(repository=self.work_repository)
        self.order_service = OrderService(repository=self.order_repository)


service_container = ServiceContainer()
