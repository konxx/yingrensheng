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
import com.yingrensheng.core.model.material.MaterialItem
import com.yingrensheng.core.model.material.MaterialType
import com.yingrensheng.core.model.project.CreationMode
import com.yingrensheng.core.network.NetworkApiResponse
import com.yingrensheng.core.network.SimpleApiClient
import com.yingrensheng.core.network.YrsApiConfig
import com.yingrensheng.core.network.requireData
import com.yingrensheng.data.project.repository.ProjectRepositoryProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking

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

    override fun interviewPrompts(): List<InterviewPrompt> {
        return listOf(
            InterviewPrompt("prompt_1", "这支片子最想送给谁？", "一句话写清对象，AI 会更容易拿捏语气。"),
            InterviewPrompt("prompt_2", "你最想保留的一个瞬间是什么？", "比如一句话、一个地方、一次拥抱。"),
            InterviewPrompt("prompt_3", "希望成片更温暖、热烈还是庄重？", "这会同时影响文案、配乐和节奏。"),
        )
    }

    override fun styles(): List<NarrativeStyle> {
        return listOf(
            NarrativeStyle("style_warm", "温暖胶片", "更柔和的旁白与偏金色的记忆感"),
            NarrativeStyle("style_youth", "青春节奏", "切点更快，更适合旅行与毕业"),
            NarrativeStyle("style_memory", "沉静回忆", "适合人生回忆与家庭纪念"),
        )
    }

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

    override fun replaceMaterials(materials: List<com.yingrensheng.core.model.material.MaterialItem>) {
        sessionState.value = sessionState.value.copy(materials = materials)
    }

    override fun updateThemeLine(themeLine: String) {
        sessionState.value = sessionState.value.copy(themeLine = themeLine)
    }

    override fun answerPrompt(promptId: String, answer: String) {
        sessionState.value = sessionState.value.copy(
            interviewAnswers = sessionState.value.interviewAnswers + (promptId to answer),
        )
    }

    override fun selectStyle(style: NarrativeStyle) {
        sessionState.value = sessionState.value.copy(selectedStyle = style)
    }

    override fun generateStoryDraft() {
        val projectId = ProjectRepositoryProvider.current.latestProject()?.projectId
        if (projectId == null) return

        runBlocking(Dispatchers.IO) {
            runCatching {
                val taskType = object : TypeToken<NetworkApiResponse<TaskPayload>>() {}.type
                val taskEnvelope: NetworkApiResponse<TaskPayload> = apiClient.post(
                    path = "/projects/$projectId/story-draft/generate",
                    body = mapOf(
                        "styleId" to (sessionState.value.selectedStyle?.styleId ?: "style_warm"),
                        "themeLine" to sessionState.value.themeLine.ifBlank { "想把那天的晚风和笑声留下来" },
                    ),
                    type = taskType,
                )
                val task = taskEnvelope.requireData()

                val draftType = object : TypeToken<NetworkApiResponse<StoryDraftPayload>>() {}.type
                val draftEnvelope: NetworkApiResponse<StoryDraftPayload> = apiClient.get(
                    "/projects/$projectId/story-draft",
                    draftType,
                )
                val draft = draftEnvelope.requireData()

                sessionState.value = sessionState.value.copy(
                    storyDraft = StoryDraft(
                        title = draft.title,
                        opening = draft.opening,
                        body = draft.body,
                        closing = draft.closing,
                    ),
                    renderTask = RenderTask(
                        taskId = task.taskId,
                        stage = task.currentStage,
                        progress = task.progress,
                        estimatedRemainingSeconds = task.estimatedRemainingSeconds,
                    ),
                )
            }.onFailure {
                sessionState.value = sessionState.value.copy(
                    renderTask = RenderTask(
                        taskId = "task_story_error",
                        stage = "故事草稿生成失败",
                        progress = 0,
                        estimatedRemainingSeconds = 0,
                    ),
                )
            }
        }
    }

    override fun buildStoryboard() {
        val projectId = ProjectRepositoryProvider.current.latestProject()?.projectId
        if (projectId == null) return
        runBlocking(Dispatchers.IO) {
            runCatching {
                val taskType = object : TypeToken<NetworkApiResponse<TaskPayload>>() {}.type
                val taskEnvelope: NetworkApiResponse<TaskPayload> = apiClient.post(
                    path = "/projects/$projectId/storyboard/generate",
                    body = emptyMap<String, String>(),
                    type = taskType,
                )
                val task = taskEnvelope.requireData()

                val sectionType = object : TypeToken<NetworkApiResponse<List<StoryboardPayload>>>() {}.type
                val sectionEnvelope: NetworkApiResponse<List<StoryboardPayload>> = apiClient.get(
                    "/projects/$projectId/storyboard",
                    sectionType,
                )
                val sections = sectionEnvelope.requireData().sortedBy { it.orderIndex }.map {
                    StoryboardSection(
                        sectionId = it.sectionId,
                        title = it.title,
                        summary = it.summary,
                        subtitleLine = it.subtitleLine,
                        durationLabel = it.durationLabel,
                    )
                }

                sessionState.value = sessionState.value.copy(
                    storyboard = sections,
                    renderTask = RenderTask(
                        taskId = task.taskId,
                        stage = task.currentStage,
                        progress = task.progress,
                        estimatedRemainingSeconds = task.estimatedRemainingSeconds,
                    ),
                )
            }.onFailure {
                sessionState.value = sessionState.value.copy(
                    renderTask = RenderTask(
                        taskId = "task_storyboard_error",
                        stage = "故事板生成失败",
                        progress = 0,
                        estimatedRemainingSeconds = 0,
                    ),
                )
            }
        }
    }

    override fun createPreview() {
        val projectId = ProjectRepositoryProvider.current.latestProject()?.projectId
        if (projectId == null) return
        runBlocking(Dispatchers.IO) {
            runCatching {
                val taskType = object : TypeToken<NetworkApiResponse<TaskPayload>>() {}.type
                val taskEnvelope: NetworkApiResponse<TaskPayload> = apiClient.post(
                    path = "/projects/$projectId/preview/generate",
                    body = emptyMap<String, String>(),
                    type = taskType,
                )
                val task = taskEnvelope.requireData()

                val previewType = object : TypeToken<NetworkApiResponse<PreviewPayload>>() {}.type
                val previewEnvelope: NetworkApiResponse<PreviewPayload> = apiClient.get(
                    "/projects/$projectId/preview",
                    previewType,
                )
                val preview = previewEnvelope.requireData()

                sessionState.value = sessionState.value.copy(
                    previewAsset = PreviewAsset(
                        title = preview.title,
                        subtitleSummary = preview.subtitleSummary,
                        musicLabel = preview.musicLabel,
                        coverCaption = preview.coverCaption,
                    ),
                    renderTask = RenderTask(
                        taskId = task.taskId,
                        stage = task.currentStage,
                        progress = task.progress,
                        estimatedRemainingSeconds = task.estimatedRemainingSeconds,
                    ),
                )
            }.onFailure {
                sessionState.value = sessionState.value.copy(
                    renderTask = RenderTask(
                        taskId = "task_preview_error",
                        stage = "预览生成失败",
                        progress = 0,
                        estimatedRemainingSeconds = 0,
                    ),
                )
            }
        }
    }

    override fun selectExportPlan(plan: ExportPlan) {
        sessionState.value = sessionState.value.copy(selectedExportPlan = plan)
    }

    override fun startExport() {
        val selectedPlan = sessionState.value.selectedExportPlan ?: return
        val projectId = ProjectRepositoryProvider.current.latestProject()?.projectId ?: return
        runBlocking(Dispatchers.IO) {
            runCatching {
                val taskType = object : TypeToken<NetworkApiResponse<TaskPayload>>() {}.type
                val taskEnvelope: NetworkApiResponse<TaskPayload> = apiClient.post(
                    path = "/exports/$projectId",
                    body = mapOf(
                        "exportPlanId" to selectedPlan.planId,
                        "resolution" to "1080P",
                        "removeWatermark" to true,
                    ),
                    type = taskType,
                )
                val task = taskEnvelope.requireData()
                sessionState.value = sessionState.value.copy(
                    renderTask = RenderTask(
                        taskId = task.taskId,
                        stage = task.currentStage,
                        progress = task.progress,
                        estimatedRemainingSeconds = task.estimatedRemainingSeconds,
                    ),
                )
            }.onFailure {
                sessionState.value = sessionState.value.copy(
                    renderTask = RenderTask(
                        taskId = "task_export_error",
                        stage = "导出任务创建失败",
                        progress = 0,
                        estimatedRemainingSeconds = 0,
                    ),
                )
            }
        }
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

private data class StoryDraftPayload(
    val projectId: String,
    val title: String,
    val opening: String,
    val body: String,
    val closing: String,
    val updatedAt: String,
)

private data class StoryboardPayload(
    val sectionId: String,
    val projectId: String,
    val orderIndex: Int,
    val title: String,
    val summary: String,
    val subtitleLine: String,
    val durationLabel: String,
)

private data class PreviewPayload(
    val projectId: String,
    val title: String,
    val subtitleSummary: String,
    val musicLabel: String,
    val coverCaption: String,
    val videoUrl: String,
    val coverUrl: String,
    val updatedAt: String,
)
