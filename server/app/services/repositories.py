from dataclasses import dataclass, field

from app.core.time import now_iso
from app.schemas.auth import LoginResponseData
from app.schemas.common import PageResponse
from app.schemas.project import CreateProjectRequest, ProjectDetail
from app.schemas.scene import SceneListItem
from app.schemas.task import TaskStatusResponse
from app.schemas.upload import UploadInitiateRequest, UploadInitiateResponse, UploadStatusResponse
from app.schemas.user import MemberMeResponse, UserProfile


@dataclass
class AuthService:
    def send_sms(self, phone: str, purpose: str) -> None:
        _ = (phone, purpose)

    def login(self, phone: str, sms_code: str, device_id: str) -> LoginResponseData:
        _ = (sms_code, device_id)
        return LoginResponseData(
            accessToken="token_dev_001",
            refreshToken="refresh_dev_001",
            expiresInSeconds=7200,
            user=UserProfile(
                userId="user_001",
                nickname="林青",
                phone=phone,
                avatarUrl="",
            ),
        )


@dataclass
class UserService:
    def get_current_user(self) -> UserProfile:
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


@dataclass
class ProjectService:
    projects: dict[str, ProjectDetail] = field(default_factory=dict)

    def __post_init__(self) -> None:
        if not self.projects:
            seeded = ProjectDetail(
                projectId="project_seed_001",
                sceneId="scene_travel",
                sceneTitle="旅行纪念",
                mode="QUICK_FILM",
                title="大理五月风",
                status="PREVIEW_READY",
                progress=100,
                currentStep="首版已生成",
                materialCount=24,
                updatedAt=now_iso(),
            )
            self.projects[seeded.project_id] = seeded

    def create_project(self, payload: CreateProjectRequest) -> ProjectDetail:
        scene_title = {
            "scene_travel": "旅行纪念",
            "scene_memory": "人生回忆",
            "scene_festival": "节庆祝福",
            "scene_hero": "主角故事",
        }.get(payload.scene_id, payload.scene_id)
        project = ProjectDetail(
            projectId=f"project_{len(self.projects) + 1:03d}",
            sceneId=payload.scene_id,
            sceneTitle=scene_title,
            mode=payload.mode,
            title=payload.title,
            status="DRAFT",
            progress=8,
            currentStep="已创建草稿",
            materialCount=0,
            updatedAt=now_iso(),
        )
        self.projects[project.project_id] = project
        return project

    def list_projects(self, status: str | None, page: int, page_size: int) -> PageResponse[ProjectDetail]:
        items = list(self.projects.values())
        if status:
            items = [item for item in items if item.status == status]
        start = max(page - 1, 0) * page_size
        sliced = items[start : start + page_size]
        return PageResponse[ProjectDetail](
            items=sliced,
            page=page,
            pageSize=page_size,
            hasMore=start + page_size < len(items),
            nextCursor=None,
        )

    def get_project(self, project_id: str) -> ProjectDetail | None:
        return self.projects.get(project_id)


@dataclass
class UploadService:
    uploads: dict[str, UploadStatusResponse] = field(default_factory=dict)

    def initiate_upload(self, payload: UploadInitiateRequest) -> UploadInitiateResponse:
        upload_id = f"upload_{len(self.uploads) + 1:03d}"
        self.uploads[upload_id] = UploadStatusResponse(
            uploadId=upload_id,
            status="PENDING",
            uploadedPartCount=0,
            totalPartCount=max(1, (payload.size_bytes + 5242879) // 5242880),
            deduplicated=False,
            materialId=None,
        )
        return UploadInitiateResponse(
            uploadId=upload_id,
            storageProvider="S3_COMPATIBLE",
            bucket="yingrensheng-materials",
            objectKey=f"{payload.project_id}/{upload_id}/{payload.file_name}",
            uploadMode="MULTIPART",
            partSizeBytes=5242880,
            alreadyUploaded=False,
            presignedUrls=[],
        )

    def get_upload(self, upload_id: str) -> UploadStatusResponse | None:
        return self.uploads.get(upload_id)


@dataclass
class TaskService:
    tasks: dict[str, TaskStatusResponse] = field(default_factory=dict)

    def __post_init__(self) -> None:
        if not self.tasks:
            task = TaskStatusResponse(
                taskId="task_story_seed_001",
                status="RUNNING",
                progress=72,
                currentStage="GENERATING_STORYBOARD",
                estimatedRemainingSeconds=45,
                result=None,
                error=None,
                updatedAt=now_iso(),
            )
            self.tasks[task.task_id] = task

    def get_task(self, task_id: str) -> TaskStatusResponse | None:
        return self.tasks.get(task_id)


@dataclass
class ServiceContainer:
    auth_service: AuthService = field(default_factory=AuthService)
    user_service: UserService = field(default_factory=UserService)
    scene_service: SceneService = field(default_factory=SceneService)
    project_service: ProjectService = field(default_factory=ProjectService)
    upload_service: UploadService = field(default_factory=UploadService)
    task_service: TaskService = field(default_factory=TaskService)


service_container = ServiceContainer()
