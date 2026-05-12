package com.yingrensheng.data.project.repository

import com.google.gson.reflect.TypeToken
import com.yingrensheng.core.model.project.CreationMode
import com.yingrensheng.core.model.project.Project
import com.yingrensheng.core.model.project.ProjectStatus
import com.yingrensheng.core.network.NetworkApiResponse
import com.yingrensheng.core.network.NetworkPage
import com.yingrensheng.core.network.SimpleApiClient
import com.yingrensheng.core.network.YrsApiConfig
import com.yingrensheng.core.network.requireData
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking

class NetworkProjectRepository(
    private val apiClient: SimpleApiClient,
    private val fallback: ProjectRepository,
) : ProjectRepository {
    private val projectsState = MutableStateFlow<List<Project>>(emptyList())

    override fun observeProjects(): StateFlow<List<Project>> {
        refreshProjects()
        return projectsState.asStateFlow()
    }

    override fun latestProject(): Project? {
        refreshProjects()
        return projectsState.value.firstOrNull() ?: fallback.latestProject()
    }

    override fun createDraft(title: String, sceneType: String, mode: CreationMode): Project {
        return runBlocking(Dispatchers.IO) {
            runCatching {
            val sceneId = when (sceneType) {
                "西游记角色穿越" -> "scene_character_xiyou"
                "红楼梦角色穿越" -> "scene_character_honglou"
                "融合历史小说" -> "scene_outline_history"
                "原创故事小说" -> "scene_outline_original"
                "生成连环漫画" -> "scene_media_comic"
                "生成短视频" -> "scene_media_short_video"
                else -> "scene_outline_original"
            }
            val responseType = object : TypeToken<NetworkApiResponse<ProjectPayload>>() {}.type
            val envelope: NetworkApiResponse<ProjectPayload> = apiClient.post(
                path = "/projects",
                body = mapOf(
                    "sceneId" to sceneId,
                    "mode" to mode.name,
                    "title" to title,
                ),
                type = responseType,
            )
            val payload = envelope.requireData()
            val project = payload.toModel()
            projectsState.value = listOf(project) + projectsState.value.filterNot { it.projectId == project.projectId }
            project
            }.getOrElse {
                val project = fallback.createDraft(title = title, sceneType = sceneType, mode = mode)
                projectsState.value = fallback.observeProjects().value
                project
            }
        }
    }

    private fun refreshProjects() {
        runBlocking(Dispatchers.IO) {
            runCatching {
                val responseType = object : TypeToken<NetworkApiResponse<NetworkPage<ProjectPayload>>>() {}.type
                val envelope: NetworkApiResponse<NetworkPage<ProjectPayload>> = apiClient.get(
                    path = "/projects?page=1&pageSize=20",
                    type = responseType,
                )
                val payload = envelope.requireData()
                projectsState.value = payload.items.map { it.toModel() }
            }.onFailure {
                projectsState.value = fallback.observeProjects().value
            }
        }
    }

    companion object {
        fun fallbackAware(): ProjectRepository {
            val fake = FakeProjectRepository()
            return NetworkProjectRepository(
                apiClient = SimpleApiClient(YrsApiConfig.DefaultBaseUrl),
                fallback = fake,
            )
        }
    }
}

private data class ProjectPayload(
    val projectId: String,
    val sceneId: String,
    val sceneTitle: String,
    val mode: String,
    val title: String,
    val status: String,
    val progress: Int,
    val currentStep: String,
    val materialCount: Int,
    val updatedAt: String,
)

private fun ProjectPayload.toModel(): Project {
    return Project(
        projectId = projectId,
        title = title,
        sceneType = sceneTitle,
        mode = runCatching { CreationMode.valueOf(mode) }.getOrDefault(CreationMode.CHARACTER_TIME_TRAVEL),
        status = when (status) {
            "PREVIEW_READY" -> ProjectStatus.PREVIEW_READY
            "GENERATING" -> ProjectStatus.GENERATING
            "EXPORTED" -> ProjectStatus.EXPORTED
            else -> ProjectStatus.DRAFT
        },
        progress = progress,
        currentStep = currentStep,
        materialCount = materialCount,
        moodLabel = when (mode) {
            "CHARACTER_TIME_TRAVEL" -> "角色穿越"
            "OUTLINE_STORY" -> "故事创作"
            "NOVEL_TO_MEDIA" -> "小说成片"
            else -> "后端同步"
        },
        updatedAt = runCatching { Instant.parse(updatedAt) }.getOrDefault(Instant.now()),
    )
}
