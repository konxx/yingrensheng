package com.yingrensheng.data.creation.repository

import com.yingrensheng.core.common.util.IdGenerator
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
import com.yingrensheng.data.project.repository.ProjectRepository
import com.yingrensheng.data.project.repository.ProjectRepositoryProvider
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface CreationRepository {
    fun observeSession(): StateFlow<CreationSession>

    fun creationModes(): List<Pair<CreationMode, String>>

    fun scenes(): List<SceneTemplate>

    fun sampleMaterials(): List<MaterialItem>

    fun interviewPrompts(): List<InterviewPrompt>

    fun styles(): List<NarrativeStyle>

    fun exportPlans(): List<ExportPlan>

    fun selectMode(mode: CreationMode)

    fun selectScene(scene: SceneTemplate)

    fun importMaterials()

    fun updateThemeLine(themeLine: String)

    fun answerPrompt(promptId: String, answer: String)

    fun selectStyle(style: NarrativeStyle)

    fun generateStoryDraft()

    fun buildStoryboard()

    fun createPreview()

    fun selectExportPlan(plan: ExportPlan)

    fun startExport()
}

object CreationRepositoryProvider {
    @Volatile
    var current: CreationRepository = NetworkCreationRepository.fallbackAware()
}

class FakeCreationRepository(
    private val projectRepository: ProjectRepository,
) : CreationRepository {
    private val sessionState = kotlinx.coroutines.flow.MutableStateFlow(CreationSession())

    override fun observeSession(): StateFlow<CreationSession> = sessionState.asStateFlow()

    override fun creationModes(): List<Pair<CreationMode, String>> = listOf(
        CreationMode.QUICK_FILM to "快速成片",
        CreationMode.STORY_FILM to "故事成片",
    )

    override fun scenes(): List<SceneTemplate> = listOf(
        SceneTemplate("scene_travel", "旅行纪念", "把风景、人物和那一天的心情剪成短片", "30-60 秒", "约 2 分钟"),
        SceneTemplate("scene_memory", "人生回忆", "适合老照片、录音和家庭故事整理", "60-180 秒", "约 4 分钟"),
        SceneTemplate("scene_festival", "节庆祝福", "生日、毕业、纪念日都能快速出片", "30-45 秒", "约 2 分钟"),
        SceneTemplate("scene_hero", "主角故事", "更强调人物表达与情绪推进", "45-90 秒", "约 3 分钟"),
    )

    override fun sampleMaterials(): List<MaterialItem> = listOf(
        MaterialItem("material_001", "洱海骑行", MaterialType.VIDEO, "00:18", "已识别到风景高光片段"),
        MaterialItem("material_002", "晚霞合照", MaterialType.PHOTO, "-", "已匹配暖色氛围封面"),
        MaterialItem("material_003", "一句旁白", MaterialType.AUDIO, "00:12", "已完成转写"),
        MaterialItem("material_004", "一句主题", MaterialType.NOTE, "-", "适合用作结尾字幕"),
    )

    override fun interviewPrompts(): List<InterviewPrompt> = listOf(
        InterviewPrompt("prompt_1", "这支片子最想送给谁？", "一句话写清对象，AI 会更容易拿捏语气。"),
        InterviewPrompt("prompt_2", "你最想保留的一个瞬间是什么？", "比如一句话、一个地方、一次拥抱。"),
        InterviewPrompt("prompt_3", "希望成片更温暖、热烈还是庄重？", "这会同时影响文案、配乐和节奏。"),
    )

    override fun styles(): List<NarrativeStyle> = listOf(
        NarrativeStyle("style_warm", "温暖胶片", "更柔和的旁白与偏金色的记忆感"),
        NarrativeStyle("style_youth", "青春节奏", "切点更快，更适合旅行与毕业"),
        NarrativeStyle("style_memory", "沉静回忆", "适合人生回忆与家庭纪念"),
    )

    override fun exportPlans(): List<ExportPlan> = listOf(
        ExportPlan("plan_single", "1080P 单次导出", "¥39.90", listOf("无水印", "可下载", "保留封面")),
        ExportPlan("plan_member", "会员解锁", "¥168/年", listOf("全年导出优惠", "高级配音", "优先渲染")),
    )

    override fun selectMode(mode: CreationMode) {
        sessionState.value = sessionState.value.copy(mode = mode)
    }

    override fun selectScene(scene: SceneTemplate) {
        val project = projectRepository.createDraft(
            title = when (scene.title) {
                "人生回忆" -> "未命名人生回忆"
                else -> "未命名${scene.title}"
            },
            sceneType = scene.title,
            mode = sessionState.value.mode ?: CreationMode.QUICK_FILM,
        )
        sessionState.value = sessionState.value.copy(
            selectedScene = scene,
            currentProjectId = project.projectId,
        )
    }

    override fun importMaterials() {
        sessionState.value = sessionState.value.copy(materials = sampleMaterials())
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
        val sceneTitle = sessionState.value.selectedScene?.title ?: "人生片段"
        val mood = sessionState.value.selectedStyle?.title ?: "温暖胶片"
        sessionState.value = sessionState.value.copy(
            storyDraft = StoryDraft(
                title = "${sceneTitle} | 第一版故事",
                opening = "从一张张零散素材里，先把最想留住的那一刻轻轻托出来。",
                body = "AI 已按“相遇 - 高光 - 留白”的结构整理素材，并把情绪收进 $mood 的旁白语气里。",
                closing = "最后留下的不只是风景，而是你那天真正想讲给别人听的话。",
            ),
            renderTask = RenderTask(
                taskId = IdGenerator.newId("task"),
                stage = "故事生成完成，正在编排分镜",
                progress = 72,
                estimatedRemainingSeconds = 45,
            ),
        )
    }

    override fun buildStoryboard() {
        sessionState.value = sessionState.value.copy(
            storyboard = listOf(
                StoryboardSection("board_1", "开场", "先落一帧最有情绪的封面镜头", "字幕：把风吹过的那一刻留下来", "00:10"),
                StoryboardSection("board_2", "高光", "把骑行、笑声和晚霞放在同一段情绪峰值里", "字幕：那些一路向前的画面都在发光", "00:22"),
                StoryboardSection("board_3", "收束", "用一句旁白和一张安静合照收尾", "字幕：原来最想记住的是一起在场", "00:16"),
            ),
        )
    }

    override fun createPreview() {
        sessionState.value = sessionState.value.copy(
            previewAsset = PreviewAsset(
                title = sessionState.value.storyDraft?.title ?: "首版成片",
                subtitleSummary = "已生成三段式故事板，支持换音乐、换封面、重写某段旁白。",
                musicLabel = "配乐：温暖胶片",
                coverCaption = "封面建议：晚霞合照 + 手写标题",
            ),
        )
    }

    override fun selectExportPlan(plan: ExportPlan) {
        sessionState.value = sessionState.value.copy(selectedExportPlan = plan)
    }

    override fun startExport() {
        sessionState.value = sessionState.value.copy(
            renderTask = RenderTask(
                taskId = IdGenerator.newId("export"),
                stage = "正在生成 1080P 导出文件",
                progress = 84,
                estimatedRemainingSeconds = 28,
            ),
        )
    }
}
