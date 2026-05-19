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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

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

    fun replaceMaterials(materials: List<MaterialItem>)

    fun updateThemeLine(themeLine: String)

    fun answerPrompt(promptId: String, answer: String)

    fun selectStyle(style: NarrativeStyle)

    suspend fun generateStoryDraft()

    suspend fun buildStoryboard()

    suspend fun createPreview()

    fun selectExportPlan(plan: ExportPlan)

    suspend fun startExport()
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
        CreationMode.CHARACTER_TIME_TRAVEL to "角色穿越",
        CreationMode.OUTLINE_STORY to "大纲成文",
        CreationMode.NOVEL_TO_MEDIA to "小说成片",
    )

    override fun scenes(): List<SceneTemplate> = listOf(
        SceneTemplate("scene_character_xiyou", "西游记角色穿越", "上传自拍，把用户架空成唐僧、女儿国行者或取经路上的新角色", "角色海报 + 短篇故事", "约 2 分钟"),
        SceneTemplate("scene_character_honglou", "红楼梦角色穿越", "把人物放进贾府关系网，生成贾宝玉式公子或新入园人物", "角色设定 + 剧情片段", "约 2 分钟"),
        SceneTemplate("scene_outline_history", "融合历史小说", "把用户大纲融入名著、朝代、宫廷、武侠或架空历史", "短篇小说 + 分镜", "约 3 分钟"),
        SceneTemplate("scene_outline_original", "原创故事小说", "从一句灵感扩写成专属世界观、人物关系和章节梗概", "短篇小说 + 连载大纲", "约 3 分钟"),
        SceneTemplate("scene_media_comic", "生成连环漫画", "解析小说人物、场景和情节节点，输出 6-12 格漫画分镜", "连环漫画脚本", "约 4 分钟"),
        SceneTemplate("scene_media_short_video", "生成短视频", "把小说拆成镜头、旁白、字幕和封面建议，适合推文视频", "30-90 秒短视频", "约 4 分钟"),
    )

    override fun sampleMaterials(): List<MaterialItem> {
        return when (sessionState.value.mode) {
            CreationMode.CHARACTER_TIME_TRAVEL -> listOf(
                MaterialItem("material_role_photo", "用户自拍参考", MaterialType.PHOTO, "-", "将用于角色脸型、气质和服饰方向参考"),
                MaterialItem("material_role_note", "角色愿望", MaterialType.NOTE, "-", "已记录用户想进入的文学世界和人物身份"),
            )
            CreationMode.OUTLINE_STORY -> listOf(
                MaterialItem("material_outline_note", "故事大纲", MaterialType.NOTE, "-", "将扩写为世界观、人物小传和短篇正文"),
            )
            CreationMode.NOVEL_TO_MEDIA -> listOf(
                MaterialItem("material_novel_text", "小说正文", MaterialType.NOTE, "-", "将拆解为人物、场景、镜头与旁白"),
            )
            else -> listOf(
                MaterialItem("material_seed_note", "创作说明", MaterialType.NOTE, "-", "已记录本次 AI 创作输入"),
            )
        }
    }

    override fun interviewPrompts(): List<InterviewPrompt> = listOf(
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

    override fun styles(): List<NarrativeStyle> = listOf(
        NarrativeStyle("style_classic", "古典章回", "适合名著融合，语言更有章回小说和古风叙事感"),
        NarrativeStyle("style_cinematic", "影视短剧", "镜头感更强，适合生成短视频旁白和分镜"),
        NarrativeStyle("style_comic", "连环漫画", "画面切分更清楚，适合 6-12 格漫画脚本"),
        NarrativeStyle("style_webnovel", "网文爽感", "节奏更快，冲突更直接，适合原创故事和连载开篇"),
    )

    override fun exportPlans(): List<ExportPlan> = listOf(
        ExportPlan("plan_single", "单次高清导出", "¥39.90", listOf("无水印", "可下载", "保留封面与字幕")),
        ExportPlan("plan_member", "创作会员", "¥168/年", listOf("更多生成额度", "角色一致性优先", "高清导出优惠")),
    )

    fun adminExportPlan(): ExportPlan = ExportPlan(
        planId = "plan_admin",
        title = "系统管理员权益导出",
        priceLabel = "已豁免",
        benefits = listOf("无水印", "高清导出", "不消耗额度", "无需支付"),
    )

    override fun selectMode(mode: CreationMode) {
        sessionState.value = CreationSession(mode = mode)
    }

    override fun selectScene(scene: SceneTemplate) {
        val project = projectRepository.createDraft(
            title = "未命名${scene.title}",
            sceneType = scene.title,
            mode = sessionState.value.mode ?: CreationMode.CHARACTER_TIME_TRAVEL,
        )
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
            currentProjectId = project.projectId,
        )
    }

    override fun importMaterials() {
        sessionState.value = sessionState.value.copy(materials = sampleMaterials())
    }

    override fun replaceMaterials(materials: List<MaterialItem>) {
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

    override suspend fun generateStoryDraft() {
        withContext(Dispatchers.Default) {
        val sceneTitle = sessionState.value.selectedScene?.title ?: "人生片段"
        val style = sessionState.value.selectedStyle?.title ?: "影视短剧"
        val input = sessionState.value.themeLine.ifBlank { "一个现代人进入古典小说世界，改写自己和主角的命运。" }
        val answers = interviewPrompts().mapNotNull { prompt ->
            sessionState.value.interviewAnswers[prompt.promptId]
                ?.takeIf { it.isNotBlank() }
                ?.let { "${prompt.title}：$it" }
        }.joinToString("；")
        sessionState.value = sessionState.value.copy(
            storyDraft = StoryDraft(
                title = "${sceneTitle} | 第一版创作草稿",
                opening = "角色被放入 $sceneTitle 的世界后，先用一个清晰的身份钩子建立观众兴趣。",
                body = "AI 已根据“$input”${if (answers.isNotBlank()) "，并结合“$answers”" else ""}整理出人物设定、核心冲突和三段式剧情，并按 $style 的方向预留漫画与短视频改编空间。",
                closing = "接下来会继续拆成镜头、旁白、字幕和画面提示，方便生成连环漫画或短视频首版。",
            ),
            renderTask = RenderTask(
                taskId = IdGenerator.newId("task"),
                stage = "创作草稿已生成，正在准备分镜",
                progress = 72,
                estimatedRemainingSeconds = 45,
            ),
        )
        }
    }

    override suspend fun buildStoryboard() {
        withContext(Dispatchers.Default) {
        val sceneTitle = sessionState.value.selectedScene?.title ?: "小说成片"
        sessionState.value = sessionState.value.copy(
            storyboard = listOf(
                StoryboardSection("board_1", "身份亮相", "用角色定妆照或小说开篇场景建立 $sceneTitle 的世界入口", "字幕：他一睁眼，已站在命运改写的开端", "00:08"),
                StoryboardSection("board_2", "冲突推进", "安排主角与原著人物或原创对手发生第一次正面碰撞", "字幕：旧故事没有等他，他却先改了局", "00:18"),
                StoryboardSection("board_3", "悬念收束", "用一个反转、承诺或未解谜题收尾，适合继续生成下一集", "字幕：下一回，真正的考验才开始", "00:10"),
            ),
        )
        }
    }

    override suspend fun createPreview() {
        withContext(Dispatchers.Default) {
        sessionState.value = sessionState.value.copy(
            previewAsset = PreviewAsset(
                title = sessionState.value.storyDraft?.title ?: "首版成片",
                subtitleSummary = "已生成三段式故事板，支持继续生成角色海报、漫画格或短视频预览。",
                musicLabel = "配乐：古风悬念",
                coverCaption = "封面建议：角色定妆照 + 世界观标题",
            ),
        )
        }
    }

    override fun selectExportPlan(plan: ExportPlan) {
        sessionState.value = sessionState.value.copy(selectedExportPlan = plan)
    }

    override suspend fun startExport() {
        withContext(Dispatchers.Default) {
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
}
