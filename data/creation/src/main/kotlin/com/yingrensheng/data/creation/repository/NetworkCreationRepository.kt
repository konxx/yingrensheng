package com.yingrensheng.data.creation.repository

import com.google.gson.reflect.TypeToken
import com.yingrensheng.core.model.creation.CreationSession
import com.yingrensheng.core.model.creation.ExportPlan
import com.yingrensheng.core.model.creation.InterviewPrompt
import com.yingrensheng.core.model.creation.NarrativeStyle
import com.yingrensheng.core.model.creation.PreviewAsset
import com.yingrensheng.core.model.creation.RenderTask
import com.yingrensheng.core.model.creation.SceneTemplate
import com.yingrensheng.core.model.creation.StoryDraft
import com.yingrensheng.core.model.creation.StoryboardSection
import com.yingrensheng.core.model.project.CreationMode
import com.yingrensheng.core.network.NetworkApiResponse
import com.yingrensheng.core.network.SimpleApiClient
import com.yingrensheng.core.network.YrsApiConfig
import com.yingrensheng.core.network.requireData
import com.yingrensheng.data.project.repository.ProjectRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkCreationRepository(
    private val apiClient: SimpleApiClient,
    private val fallback: CreationRepository,
) : CreationRepository {
    private val sessionState = MutableStateFlow(CreationSession())

    override fun observeSession(): StateFlow<CreationSession> = sessionState.asStateFlow()

    override fun creationModes(): List<Pair<CreationMode, String>> = fallback.creationModes()

    override fun scenes(): List<SceneTemplate> {
        return runCatching {
            val responseType = object : TypeToken<NetworkApiResponse<List<ScenePayload>>>() {}.type
            val envelope: NetworkApiResponse<List<ScenePayload>> = apiClient.get("/scenes", responseType)
            val payload: List<ScenePayload> = envelope.requireData()
            payload.map { item ->
                SceneTemplate(
                    sceneId = item.sceneId,
                    title = item.title,
                    subtitle = item.subtitle,
                    recommendedDurationLabel = item.recommendedDurationLabel,
                    estimatedTimeLabel = item.estimatedTimeLabel,
                )
            }
        }.getOrElse { fallback.scenes() }
    }

    override fun sampleMaterials() = fallback.sampleMaterials()

    override fun interviewPrompts(): List<InterviewPrompt> = fallback.interviewPrompts()

    override fun styles(): List<NarrativeStyle> = fallback.styles()

    override fun exportPlans(): List<ExportPlan> = fallback.exportPlans()

    override fun selectMode(mode: CreationMode) {
        sessionState.value = sessionState.value.copy(mode = mode)
        fallback.selectMode(mode)
    }

    override fun selectScene(scene: SceneTemplate) {
        sessionState.value = sessionState.value.copy(selectedScene = scene)
        fallback.selectScene(scene)
        sessionState.value = sessionState.value.copy(currentProjectId = fallback.observeSession().value.currentProjectId)
    }

    override fun importMaterials() = fallback.importMaterials()

    override fun updateThemeLine(themeLine: String) {
        sessionState.value = sessionState.value.copy(themeLine = themeLine)
        fallback.updateThemeLine(themeLine)
    }

    override fun answerPrompt(promptId: String, answer: String) = fallback.answerPrompt(promptId, answer)

    override fun selectStyle(style: NarrativeStyle) {
        sessionState.value = sessionState.value.copy(selectedStyle = style)
        fallback.selectStyle(style)
    }

    override fun generateStoryDraft() {
        val projectId = ProjectRepositoryProvider.current.latestProject()?.projectId
        if (projectId == null) {
            fallback.generateStoryDraft()
            sessionState.value = fallback.observeSession().value
            return
        }

        runCatching {
            val responseType = object : TypeToken<NetworkApiResponse<TaskPayload>>() {}.type
            val envelope: NetworkApiResponse<TaskPayload> = apiClient.get("/tasks/task_story_seed_001", responseType)
            val task: TaskPayload = envelope.requireData()
            sessionState.value = sessionState.value.copy(
                storyDraft = StoryDraft(
                    title = "${sessionState.value.selectedScene?.title ?: "映人生"} | 第一版故事",
                    opening = "已接入后端任务状态，故事生成会以异步任务形式返回。",
                    body = "当前展示的是数据库版任务状态回流，下一步可继续接 story-draft / storyboard 真接口。",
                    closing = "前后端分离链路已经打通第一段。",
                ),
                renderTask = RenderTask(
                    taskId = task.taskId,
                    stage = task.currentStage,
                    progress = task.progress,
                    estimatedRemainingSeconds = task.estimatedRemainingSeconds,
                ),
            )
        }.onFailure {
            fallback.generateStoryDraft()
            sessionState.value = fallback.observeSession().value
        }
    }

    override fun buildStoryboard() {
        fallback.buildStoryboard()
        sessionState.value = fallback.observeSession().value.copy(
            renderTask = sessionState.value.renderTask ?: fallback.observeSession().value.renderTask,
        )
    }

    override fun createPreview() {
        fallback.createPreview()
        sessionState.value = fallback.observeSession().value
    }

    override fun selectExportPlan(plan: ExportPlan) {
        fallback.selectExportPlan(plan)
        sessionState.value = fallback.observeSession().value
    }

    override fun startExport() {
        fallback.startExport()
        sessionState.value = fallback.observeSession().value
    }

    companion object {
        fun fallbackAware(): CreationRepository {
            val fake = FakeCreationRepository(projectRepository = ProjectRepositoryProvider.current)
            return NetworkCreationRepository(
                apiClient = SimpleApiClient(YrsApiConfig.DefaultBaseUrl),
                fallback = fake,
            )
        }
    }
}

private data class ScenePayload(
    val sceneId: String,
    val title: String,
    val subtitle: String,
    val recommendedDurationLabel: String,
    val estimatedTimeLabel: String,
)

private data class TaskPayload(
    val taskId: String,
    val status: String,
    val progress: Int,
    val currentStage: String,
    val estimatedRemainingSeconds: Int,
)
