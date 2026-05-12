package com.yingrensheng.data.project.repository

import com.yingrensheng.core.common.util.IdGenerator
import com.yingrensheng.core.model.project.CreationMode
import com.yingrensheng.core.model.project.Project
import com.yingrensheng.core.model.project.ProjectStatus
import java.time.Instant
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ProjectRepository {
    fun observeProjects(): StateFlow<List<Project>>

    fun latestProject(): Project?

    fun createDraft(title: String, sceneType: String, mode: CreationMode): Project
}

object ProjectRepositoryProvider {
    @Volatile
    var current: ProjectRepository = NetworkProjectRepository.fallbackAware()
}

class FakeProjectRepository : ProjectRepository {
    private val now = Instant.now()

    private val projectsState = kotlinx.coroutines.flow.MutableStateFlow(
        listOf(
            Project(
                projectId = "project_character",
                title = "我在女儿国当取经人",
                sceneType = "西游记角色穿越",
                mode = CreationMode.CHARACTER_TIME_TRAVEL,
                status = ProjectStatus.PREVIEW_READY,
                progress = 100,
                currentStep = "角色短剧已生成",
                materialCount = 2,
                moodLabel = "古典章回",
                updatedAt = now,
            ),
            Project(
                projectId = "project_novel",
                title = "贾府来客",
                sceneType = "融合历史小说",
                mode = CreationMode.OUTLINE_STORY,
                status = ProjectStatus.GENERATING,
                progress = 68,
                currentStep = "AI 正在拆分漫画分镜",
                materialCount = 1,
                moodLabel = "影视短剧",
                updatedAt = now.minusSeconds(3600),
            ),
        ),
    )

    override fun observeProjects(): StateFlow<List<Project>> = projectsState.asStateFlow()

    override fun latestProject(): Project? = projectsState.value.firstOrNull()

    override fun createDraft(title: String, sceneType: String, mode: CreationMode): Project {
        val newProject = Project(
            projectId = IdGenerator.newId("project"),
            title = title,
            sceneType = sceneType,
            mode = mode,
            status = ProjectStatus.DRAFT,
            progress = 8,
            currentStep = "已创建草稿",
            materialCount = 0,
            moodLabel = "待选择",
            updatedAt = Instant.now(),
        )
        projectsState.value = listOf(newProject) + projectsState.value
        return newProject
    }
}
