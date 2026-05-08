from dataclasses import dataclass, field

from app.core.time import now_iso
from app.repositories.project_repository import ProjectRepository
from app.repositories.task_repository import TaskRepository
from app.repositories.upload_repository import UploadRepository
from app.repositories.user_repository import UserRepository
from app.schemas.auth import LoginResponseData, RegisterRequest
from app.schemas.common import PageResponse
from app.schemas.project import CreateProjectRequest, ProjectDetail
from app.schemas.scene import SceneListItem
from app.schemas.task import TaskStatusResponse
from app.schemas.upload import UploadInitiateRequest, UploadInitiateResponse, UploadStatusResponse
from app.schemas.user import MemberMeResponse, UserProfile


@dataclass
class AuthService:
    repository: UserRepository

    def send_sms(self, phone: str, purpose: str) -> None:
        _ = (phone, purpose)

    def login(self, phone: str, sms_code: str, device_id: str, password: str | None = None) -> LoginResponseData:
        _ = (sms_code, device_id)
        profile = self.repository.verify_user(phone=phone, password=password or "123456")
        if profile is None:
            raise ValueError("INVALID_CREDENTIALS")
        return LoginResponseData(
            accessToken="token_dev_001",
            refreshToken="refresh_dev_001",
            expiresInSeconds=7200,
            user=profile,
        )

    def register(self, payload: RegisterRequest) -> LoginResponseData:
        existing = self.repository.get_by_phone(payload.phone)
        if existing is not None:
            raise ValueError("PHONE_ALREADY_REGISTERED")
        profile = self.repository.create_user(
            phone=payload.phone,
            nickname=payload.nickname,
            password=payload.password,
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
        profile = self.repository.verify_user(phone="13800138000", password="123456")
        if profile is not None:
            return profile
        return UserProfile(
            userId="user_001",
            nickname="林青",
            phone="13800138000",
            avatarUrl="",
        )

    def get_member_info(self) -> MemberMeResponse:
        return MemberMeResponse(
            levelName="映人生会员",
            subtitle="解锁高清导出、胶片情绪模板与优先渲染",
            remainingExportCount=3,
            activePlanPriceLabel="年卡 ¥168",
        )


@dataclass
class SceneService:
    def list_scenes(self) -> list[SceneListItem]:
        return [
            SceneListItem(
                sceneId="scene_travel",
                title="旅行纪念",
                subtitle="把风景、人物和那一天的心情剪成短片",
                recommendedDurationLabel="30-60 秒",
                estimatedTimeLabel="约 2 分钟",
                coverUrl="",
                supportedMaterialTypes=["PHOTO", "VIDEO", "AUDIO", "NOTE"],
            ),
            SceneListItem(
                sceneId="scene_memory",
                title="人生回忆",
                subtitle="适合老照片、录音和家庭故事整理",
                recommendedDurationLabel="60-180 秒",
                estimatedTimeLabel="约 4 分钟",
                coverUrl="",
                supportedMaterialTypes=["PHOTO", "VIDEO", "AUDIO", "NOTE"],
            ),
        ]

    def scene_title_map(self) -> dict[str, str]:
        return {
            "scene_travel": "旅行纪念",
            "scene_memory": "人生回忆",
            "scene_festival": "节庆祝福",
            "scene_hero": "主角故事",
        }


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
class ServiceContainer:
    user_repository: UserRepository = field(default_factory=UserRepository)
    scene_service: SceneService = field(default_factory=SceneService)
    auth_service: AuthService = field(init=False)
    user_service: UserService = field(init=False)
    project_service: ProjectService = field(init=False)
    upload_service: UploadService = field(init=False)
    task_service: TaskService = field(init=False)

    def __post_init__(self) -> None:
        self.auth_service = AuthService(repository=self.user_repository)
        self.user_service = UserService(repository=self.user_repository)
        self.project_service = ProjectService(
            repository=ProjectRepository(),
            scene_service=self.scene_service,
        )
        self.upload_service = UploadService(repository=UploadRepository())
        self.task_service = TaskService(repository=TaskRepository())


service_container = ServiceContainer()
