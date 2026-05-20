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
import com.yingrensheng.core.model.creation.requiresVideoPreview
import com.yingrensheng.core.model.creation.outputKind
import com.yingrensheng.core.model.creation.CreationOutputKind
import com.yingrensheng.core.model.material.MaterialItem
import com.yingrensheng.core.model.material.MaterialType
import com.yingrensheng.core.model.project.CreationMode
import com.yingrensheng.core.network.NetworkApiResponse
import com.yingrensheng.core.network.SimpleApiClient
import com.yingrensheng.core.network.YrsApiConfig
import com.yingrensheng.core.network.requireData
import com.yingrensheng.data.project.repository.ProjectRepositoryProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

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
            val remoteScenes = payload.map { item ->
                SceneTemplate(
                    sceneId = item.sceneId,
                    title = item.title,
                    subtitle = item.subtitle,
                    recommendedDurationLabel = item.recommendedDurationLabel,
                    estimatedTimeLabel = item.estimatedTimeLabel,
                )
            }
            if (remoteScenes.any { it.sceneId.startsWith("scene_character") }) remoteScenes else fallback.scenes()
        }.getOrElse { fallback.scenes() }
    }

    override fun sampleMaterials() = fallback.sampleMaterials()

    override fun interviewPrompts(): List<InterviewPrompt> {
        return listOf(
            when (sessionState.value.mode) {
                CreationMode.CHARACTER_TIME_TRAVEL -> InterviewPrompt("prompt_1", "希望主角更像原著人物，还是一个新角色？", "例如唐僧转世、贾府贵公子、女儿国新来客。")
                CreationMode.OUTLINE_STORY -> InterviewPrompt("prompt_1", "故事最核心的冲突是什么？", "一句话说清主角想要什么、阻力来自哪里。")
                CreationMode.NOVEL_TO_MEDIA -> InterviewPrompt("prompt_1", "这次优先做漫画还是短视频？", "如果已在上一页选择模板，也可以补充希望的节奏。")
                else -> InterviewPrompt("prompt_1", "这次最想表达什么？", "一句话写清创作目标。")
            },
            when (sessionState.value.mode) {
                CreationMode.CHARACTER_TIME_TRAVEL -> InterviewPrompt("prompt_2", "要保留用户照片里的哪些气质？", "比如清冷、少年感、温和、英气、贵气。")
                CreationMode.OUTLINE_STORY -> InterviewPrompt("prompt_2", "想要哪种时代或题材质感？", "例如唐宋、明清、民国、武侠、宫廷、修仙。")
                CreationMode.NOVEL_TO_MEDIA -> InterviewPrompt("prompt_2", "最想突出哪一个剧情节点？", "适合作为漫画第 1 格或短视频前 5 秒钩子。")
                else -> InterviewPrompt("prompt_2", "希望 AI 优先抓住哪段内容？", "例如人物、关系、转折或情绪。")
            },
            InterviewPrompt("prompt_3", "希望整体更古典、热血、悬疑还是温柔？", "这会影响文风、画风、旁白和字幕。"),
        )
    }

    override fun styles(): List<NarrativeStyle> {
        return listOf(
            NarrativeStyle("style_classic", "古典章回", "适合名著融合，语言更有章回小说和古风叙事感"),
            NarrativeStyle("style_cinematic", "影视短剧", "镜头感更强，适合生成短视频旁白和分镜"),
            NarrativeStyle("style_comic", "连环漫画", "画面切分更清楚，适合 6-12 格漫画脚本"),
            NarrativeStyle("style_webnovel", "网文爽感", "节奏更快，冲突更直接，适合原创故事和连载开篇"),
        )
    }

    override fun selectMode(mode: CreationMode) {
        sessionState.value = CreationSession(mode = mode)
        fallback.selectMode(mode)
    }

    override fun selectScene(scene: SceneTemplate) {
        sessionState.value = sessionState.value.copy(
            selectedScene = scene,
            materials = emptyList(),
            themeLine = "",
            interviewAnswers = emptyMap(),
            selectedStyle = null,
            storyDraft = null,
            storyboard = emptyList(),
            previewAsset = null,
            renderTask = null,
            selectedExportPlan = null,
        )
        fallback.selectScene(scene)
        sessionState.value = sessionState.value.copy(currentProjectId = fallback.observeSession().value.currentProjectId)
    }

    override fun importMaterials() {
        fallback.importMaterials()
        sessionState.value = fallback.observeSession().value
    }

    override fun replaceMaterials(materials: List<com.yingrensheng.core.model.material.MaterialItem>) {
        fallback.replaceMaterials(materials)
        sessionState.value = sessionState.value.copy(materials = materials)
    }

    override fun updateThemeLine(themeLine: String) {
        fallback.updateThemeLine(themeLine)
        sessionState.value = sessionState.value.copy(themeLine = themeLine)
    }

    override fun answerPrompt(promptId: String, answer: String) {
        fallback.answerPrompt(promptId, answer)
        sessionState.value = sessionState.value.copy(
            interviewAnswers = sessionState.value.interviewAnswers + (promptId to answer),
        )
    }

    override fun selectStyle(style: NarrativeStyle) {
        fallback.selectStyle(style)
        sessionState.value = sessionState.value.copy(selectedStyle = style)
    }

    override suspend fun generateStoryDraft() {
        val projectId = sessionState.value.currentProjectId ?: ProjectRepositoryProvider.current.latestProject()?.projectId
        if (projectId == null) return

        withContext(Dispatchers.IO) {
            runCatching {
                val session = sessionState.value
                val directorAnswers = interviewPrompts().mapNotNull { prompt ->
                    session.interviewAnswers[prompt.promptId]
                        ?.takeIf { it.isNotBlank() }
                        ?.let { "${prompt.title}：$it" }
                }
                val enrichedThemeLine = buildString {
                    append(session.themeLine.ifBlank { "一个现代人进入古典小说世界，改写自己和主角的命运。" })
                    if (directorAnswers.isNotEmpty()) {
                        append("\n\nAI 导演补齐设定：\n")
                        directorAnswers.forEach { answer ->
                            append("- ").append(answer).append('\n')
                        }
                    }
                    if (session.materials.isNotEmpty()) {
                        append("\n用户素材：\n")
                        session.materials.forEach { material ->
                            append("- ")
                                .append(material.title)
                                .append(" / ")
                                .append(material.type.name)
                                .append(" / ")
                                .append(material.insight)
                                .append('\n')
                        }
                    }
                }
                val taskType = object : TypeToken<NetworkApiResponse<TaskPayload>>() {}.type
                val taskEnvelope: NetworkApiResponse<TaskPayload> = apiClient.post(
                    path = "/projects/$projectId/story-draft/generate",
                    body = mapOf(
                        "styleId" to (session.selectedStyle?.styleId ?: session.defaultStyleId()),
                        "themeLine" to enrichedThemeLine,
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
                fallback.generateStoryDraft()
                sessionState.value = fallback.observeSession().value
            }
        }
    }

    override suspend fun buildStoryboard() {
        val projectId = sessionState.value.currentProjectId ?: ProjectRepositoryProvider.current.latestProject()?.projectId
        if (projectId == null) return
        withContext(Dispatchers.IO) {
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
                if (!sessionState.value.requiresVideoPreview()) {
                    runCatching {
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
                                coverUrl = preview.coverUrl,
                                videoUrl = preview.videoUrl,
                            ),
                        )
                    }
                }
            }.onFailure {
                fallback.buildStoryboard()
                sessionState.value = fallback.observeSession().value
            }
        }
    }

    override suspend fun createPreview() {
        if (!sessionState.value.requiresVideoPreview()) {
            fallback.createPreview()
            sessionState.value = fallback.observeSession().value
            return
        }
        val projectId = sessionState.value.currentProjectId ?: ProjectRepositoryProvider.current.latestProject()?.projectId
        if (projectId == null) return
        withContext(Dispatchers.IO) {
            runCatching {
                val taskType = object : TypeToken<NetworkApiResponse<TaskPayload>>() {}.type
                val taskEnvelope: NetworkApiResponse<TaskPayload> = apiClient.post(
                    path = "/projects/$projectId/preview/generate",
                    body = emptyMap<String, String>(),
                    type = taskType,
                )
                val submittedTask = taskEnvelope.requireData()
                sessionState.value = sessionState.value.copy(
                    renderTask = submittedTask.toRenderTask(),
                )
                val task = pollTaskUntilFinished(submittedTask.taskId)
                if (task.status != "SUCCEEDED") {
                    throw IllegalStateException(task.errorMessage() ?: "预览视频生成失败")
                }

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
                        coverUrl = preview.coverUrl,
                        videoUrl = preview.videoUrl,
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
                        stage = "预览生成失败：${it.toPreviewErrorMessage()}",
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

    override suspend fun startExport() {
        val selectedPlan = sessionState.value.selectedExportPlan ?: return
        val projectId = sessionState.value.currentProjectId ?: ProjectRepositoryProvider.current.latestProject()?.projectId ?: return
        val kind = sessionState.value.outputKind()
        withContext(Dispatchers.IO) {
            runCatching {
                val taskType = object : TypeToken<NetworkApiResponse<TaskPayload>>() {}.type
                val taskEnvelope: NetworkApiResponse<TaskPayload> = apiClient.post(
                    path = "/exports/$projectId",
                    body = mapOf(
                        "exportPlanId" to selectedPlan.planId,
                        "resolution" to kind.exportResolutionLabel(),
                        "removeWatermark" to (kind == CreationOutputKind.SHORT_VIDEO),
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
                        stage = "导出任务创建失败：${it.toExportErrorMessage()}",
                        progress = 0,
                        estimatedRemainingSeconds = 0,
                    ),
                )
            }
        }
    }

    override fun exportPlans(outputKind: CreationOutputKind): List<ExportPlan> = fallback.exportPlans(outputKind)

    companion object {
        fun fallbackAware(): CreationRepository {
            val fake = FakeCreationRepository(projectRepository = ProjectRepositoryProvider.current)
            return NetworkCreationRepository(
                apiClient = SimpleApiClient(),
                fallback = fake,
            )
        }
    }

    private suspend fun pollTaskUntilFinished(taskId: String): TaskPayload {
        val taskType = object : TypeToken<NetworkApiResponse<TaskPayload>>() {}.type
        var latest: TaskPayload? = null
        repeat(40) {
            val envelope: NetworkApiResponse<TaskPayload> = apiClient.get(
                "/tasks/$taskId",
                taskType,
            )
            val task = envelope.requireData()
            latest = task
            sessionState.value = sessionState.value.copy(renderTask = task.toRenderTask())
            if (task.status == "SUCCEEDED" || task.status == "FAILED") {
                return task
            }
            delay(3_000)
        }
        return latest ?: throw IllegalStateException("预览任务查询超时")
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
    val result: Map<String, Any?>? = null,
    val error: Map<String, Any?>? = null,
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

private fun TaskPayload.toRenderTask(): RenderTask {
    return RenderTask(
        taskId = taskId,
        stage = currentStage.toTaskStageLabel(),
        progress = progress,
        estimatedRemainingSeconds = estimatedRemainingSeconds,
    )
}

private fun TaskPayload.errorMessage(): String? {
    return error?.get("message")?.toString()
        ?: error?.get("code")?.toString()
        ?: error?.toString()
}

private fun String.toTaskStageLabel(): String {
    return when (this) {
        "DASHSCOPE_VIDEO_RUNNING" -> "DashScope 正在生成视频预览"
        "DASHSCOPE_VIDEO_SUCCEEDED" -> "DashScope 视频预览已生成"
        "DASHSCOPE_IMAGE_FAILED" -> "DashScope 首帧图生成失败"
        "DASHSCOPE_VIDEO_SUBMIT_FAILED" -> "DashScope 视频任务提交失败"
        "DASHSCOPE_VIDEO_FAILED" -> "DashScope 视频生成失败"
        "DASHSCOPE_PREVIEW_FAILED" -> "DashScope 预览生成失败"
        else -> this
    }
}

private fun Throwable.toPreviewErrorMessage(): String {
    val raw = message.orEmpty()
    return when {
        "DASHSCOPE_IMAGE_FAILED" in raw -> "首帧图片生成失败"
        "DASHSCOPE_VIDEO_SUBMIT_FAILED" in raw -> "视频任务提交失败"
        "DASHSCOPE_VIDEO_FAILED" in raw -> "视频生成失败"
        "HTTP 409" in raw -> "预览前置资源不完整"
        raw.isNotBlank() -> raw
        else -> "未知网络或后端异常"
    }
}

private fun Throwable.toExportErrorMessage(): String {
    val raw = message.orEmpty()
    return when {
        "ADMIN_EXPORT_REQUIRES_ADMIN" in raw -> "当前登录态不是系统管理员，请重新登录 Admin 账号"
        "PROJECT_NOT_FOUND" in raw -> "当前创作项目没有同步到后端，请重新从场景选择创建"
        "EXPORT_PREREQUISITES_MISSING" in raw -> "预览资源未生成成功，请先重新生成首版预览"
        "HTTP 403" in raw -> "没有该导出权益"
        "HTTP 404" in raw -> "后端找不到当前项目"
        "HTTP 409" in raw -> "导出前置资源不完整"
        raw.isNotBlank() -> raw
        else -> "未知网络或后端异常"
    }
}

private fun CreationSession.defaultStyleId(): String {
    return when (selectedScene?.sceneId) {
        "scene_character_xiyou" -> "style_classic"
        "scene_character_honglou" -> "style_classic"
        "scene_outline_history" -> "style_classic"
        "scene_outline_original" -> "style_webnovel"
        "scene_media_comic" -> "style_comic"
        "scene_media_short_video" -> "style_cinematic"
        else -> "style_cinematic"
    }
}

private fun CreationOutputKind.exportResolutionLabel(): String {
    return when (this) {
        CreationOutputKind.STORY_TEXT -> "TEXT_PACKAGE"
        CreationOutputKind.COMIC_STORYBOARD -> "COMIC_STORYBOARD_PACKAGE"
        CreationOutputKind.CHARACTER_STORY -> "CHARACTER_STORY_PACKAGE"
        CreationOutputKind.SHORT_VIDEO -> "1080P"
    }
}
