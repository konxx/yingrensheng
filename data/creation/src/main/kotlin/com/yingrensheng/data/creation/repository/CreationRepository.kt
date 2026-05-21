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
import com.yingrensheng.core.model.creation.outputKind
import com.yingrensheng.core.model.creation.CreationOutputKind
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

    fun exportPlans(outputKind: CreationOutputKind = observeSession().value.outputKind()): List<ExportPlan>

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
        CreationMode.NOVEL_TO_MEDIA to "小说改编",
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

    override fun exportPlans(outputKind: CreationOutputKind): List<ExportPlan> {
        return when (outputKind) {
            CreationOutputKind.STORY_TEXT -> listOf(
                ExportPlan("plan_story_single", "单次小说创作包", "¥19.90", listOf("完整小说正文", "分章正文", "人物关系资料", "无视频生成")),
                ExportPlan("plan_story_member", "小说创作会员", "¥98/年", listOf("更多文本生成额度", "支持多版改写", "章节正文长期保存")),
            )
            CreationOutputKind.COMIC_STORYBOARD -> listOf(
                ExportPlan("plan_comic_single", "单次漫画分镜包", "¥29.90", listOf("8-12 格分镜", "画面提示", "对白与旁白框", "不生成视频")),
                ExportPlan("plan_comic_member", "漫画创作会员", "¥128/年", listOf("更多分格额度", "角色一致性提示", "高清图文分镜包")),
            )
            CreationOutputKind.CHARACTER_STORY -> listOf(
                ExportPlan("plan_character_single", "单次角色故事包", "¥29.90", listOf("角色设定卡", "关系位置卡", "第一幕剧情", "扩展方向")),
                ExportPlan("plan_character_member", "角色创作会员", "¥128/年", listOf("更多角色包额度", "名著关系扩展", "后续漫画/视频改编建议")),
            )
            CreationOutputKind.SHORT_VIDEO -> listOf(
                ExportPlan("plan_single", "单次短视频导出", "¥39.90", listOf("无水印", "可下载", "保留封面与字幕")),
                ExportPlan("plan_member", "短视频创作会员", "¥168/年", listOf("更多生成额度", "角色一致性优先", "高清导出优惠")),
            )
        }
    }

    fun adminExportPlan(): ExportPlan = ExportPlan(
        planId = "plan_admin",
        title = "尊享权益导出",
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
                body = draftBodyForKind(
                    kind = sessionState.value.outputKind(),
                    input = input,
                    answers = answers,
                    style = style,
                ),
                closing = draftClosingForKind(sessionState.value.outputKind()),
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
        val sceneTitle = sessionState.value.selectedScene?.title ?: "小说改编"
        sessionState.value = sessionState.value.copy(
            storyboard = storyboardForKind(kind = sessionState.value.outputKind(), sceneTitle = sceneTitle),
        )
        }
    }

    override suspend fun createPreview() {
        withContext(Dispatchers.Default) {
        sessionState.value = sessionState.value.copy(
            previewAsset = PreviewAsset(
                title = sessionState.value.storyDraft?.title ?: "首版成果",
                subtitleSummary = when (sessionState.value.outputKind()) {
                    CreationOutputKind.STORY_TEXT -> "已生成小说创作稿和分章正文，可导出文本创作包。"
                    CreationOutputKind.COMIC_STORYBOARD -> "已生成连环漫画分镜脚本，可导出漫画分镜包。"
                    CreationOutputKind.CHARACTER_STORY -> "已生成角色故事设定和剧情分镜，可导出角色故事包。"
                    CreationOutputKind.SHORT_VIDEO -> "已生成三段式故事板，支持继续生成短视频预览。"
                },
                musicLabel = when (sessionState.value.outputKind()) {
                    CreationOutputKind.STORY_TEXT -> "输出：小说文本"
                    CreationOutputKind.COMIC_STORYBOARD -> "输出：漫画分镜"
                    CreationOutputKind.CHARACTER_STORY -> "输出：角色故事"
                    CreationOutputKind.SHORT_VIDEO -> "配乐：古风悬念"
                },
                coverCaption = when (sessionState.value.outputKind()) {
                    CreationOutputKind.STORY_TEXT -> "包含：标题、开场、正文主线、结尾"
                    CreationOutputKind.COMIC_STORYBOARD -> "包含：画面、字幕、节奏、分格建议"
                    CreationOutputKind.CHARACTER_STORY -> "包含：角色身份、故事主线、分镜方向"
                    CreationOutputKind.SHORT_VIDEO -> "封面建议：角色定妆照 + 世界观标题"
                },
            ),
        )
        }
    }

    override fun selectExportPlan(plan: ExportPlan) {
        sessionState.value = sessionState.value.copy(selectedExportPlan = plan)
    }

    override suspend fun startExport() {
        withContext(Dispatchers.Default) {
        val kind = sessionState.value.outputKind()
        sessionState.value = sessionState.value.copy(
            renderTask = RenderTask(
                taskId = IdGenerator.newId("export"),
                stage = when (kind) {
                    CreationOutputKind.STORY_TEXT -> "正在整理小说创作包"
                    CreationOutputKind.COMIC_STORYBOARD -> "正在整理漫画分镜包"
                    CreationOutputKind.CHARACTER_STORY -> "正在整理角色故事包"
                    CreationOutputKind.SHORT_VIDEO -> "正在生成 1080P 导出文件"
                },
                progress = if (kind == CreationOutputKind.SHORT_VIDEO) 84 else 100,
                estimatedRemainingSeconds = if (kind == CreationOutputKind.SHORT_VIDEO) 28 else 0,
            ),
        )
        }
    }
}

private fun draftBodyForKind(
    kind: CreationOutputKind,
    input: String,
    answers: String,
    style: String,
): String {
    val answerText = if (answers.isNotBlank()) "，并结合“$answers”" else ""
    return when (kind) {
        CreationOutputKind.STORY_TEXT -> "AI 已根据“$input”$answerText 整理出主角欲望、世界规则、人物关系和章节走向，并按 $style 的方向扩写为小说创作稿。"
        CreationOutputKind.COMIC_STORYBOARD -> "AI 已根据“$input”$answerText 提取人物、场景、关键动作和对白爆点，并按 $style 的方向准备拆成连环漫画分格。"
        CreationOutputKind.CHARACTER_STORY -> "AI 已根据“$input”$answerText 整理出角色身份、气质、原著关系和第一幕剧情，并按 $style 的方向生成角色故事包。"
        CreationOutputKind.SHORT_VIDEO -> "AI 已根据“$input”$answerText 提取前 5 秒钩子、镜头推进、旁白和字幕节奏，并按 $style 的方向准备生成短视频分镜。"
    }
}

private fun draftClosingForKind(kind: CreationOutputKind): String {
    return when (kind) {
        CreationOutputKind.STORY_TEXT -> "接下来会生成分章正文、人物关系和结尾余味，不进入视频生成。"
        CreationOutputKind.COMIC_STORYBOARD -> "接下来会拆成画面、动作、对白、旁白框和页内节奏，不进入视频生成。"
        CreationOutputKind.CHARACTER_STORY -> "接下来会整理角色身份卡、关系位置卡、第一幕剧情和扩展方向。"
        CreationOutputKind.SHORT_VIDEO -> "接下来会拆成镜头、旁白、字幕和首帧提示，并生成短视频预览。"
    }
}

private fun storyboardForKind(
    kind: CreationOutputKind,
    sceneTitle: String,
): List<StoryboardSection> {
    return when (kind) {
        CreationOutputKind.COMIC_STORYBOARD -> listOf(
            StoryboardSection("board_comic_1", "第 1 格：入画钩子", "远景建立 $sceneTitle 的时代和地点，主角从原小说关键场景中被推到画面前景。", "旁白框：旧故事翻到这一页时，他忽然成了画中人。", "第 1 页 / 上排大格"),
            StoryboardSection("board_comic_2", "第 2 格：人物亮相", "中近景刻画主角服饰、神态和手中关键物件，保留用户输入里的气质标签。", "对白：我记得这里，可书里没有我。", "第 1 页 / 右上格"),
            StoryboardSection("board_comic_3", "第 3 格：关系碰撞", "原著人物或对手入画，两人视线相撞，背景用门廊、街市或庭院压出空间层次。", "对白：你是谁，怎会知道这件事？", "第 1 页 / 中排左格"),
            StoryboardSection("board_comic_4", "第 4 格：关键动作", "主角做出第一次改变剧情的动作，画面突出手势、衣袖、道具和围观反应。", "拟声：啪。", "第 1 页 / 中排右格"),
            StoryboardSection("board_comic_5", "第 5 格：反应定格", "切到对方表情和环境细节，让读者看见这一步已经改变原本命运。", "旁白框：命数像被墨滴晕开。", "第 2 页 / 上排左格"),
            StoryboardSection("board_comic_6", "第 6 格：冲突升级", "用斜构图表现追问、误会或追逐，人物身体方向形成明确动线。", "对白：既然你知前因，就该知道后果。", "第 2 页 / 上排右格"),
            StoryboardSection("board_comic_7", "第 7 格：情绪反转", "主角发现真正代价，画面压暗，只留脸部高光或关键物件亮色。", "旁白框：他终于明白，改写不是逃离。", "第 2 页 / 中排整格"),
            StoryboardSection("board_comic_8", "第 8 格：尾页悬念", "最后一格留出下一回钩子，远处人物、信物或门后阴影引出续篇。", "字幕：下一页，旧命簿开始回看他。", "第 2 页 / 结尾大格"),
        )
        CreationOutputKind.SHORT_VIDEO -> listOf(
            StoryboardSection("board_video_1", "0-5 秒：强钩子", "用反常识台词、命运危机或身份错位直接开场，首帧必须有人物和强冲突。", "字幕：他刚进书里，就改掉了主角的命。", "0-5s"),
            StoryboardSection("board_video_2", "5-18 秒：身份交代", "镜头从道具推到人物脸部，交代主角身份、地点和与原故事的关系。", "旁白：这里所有人都按旧书活着，只有他记得结局。", "5-18s"),
            StoryboardSection("board_video_3", "18-38 秒：冲突升级", "用三到四个快切镜头呈现阻拦、追问、误会或打斗，字幕同步推进剧情。", "字幕：他救下的人，偏偏是原书里最不该活的人。", "18-38s"),
            StoryboardSection("board_video_4", "38-60 秒：反转收束", "镜头慢下来，留一个代价、秘密或下集承诺，适合导出为短视频预览。", "字幕：可他不知道，命运也在改写他。", "38-60s"),
        )
        CreationOutputKind.STORY_TEXT -> listOf(
            StoryboardSection("board_story_1", "第一章：世界入口", "建立 $sceneTitle 的时代规则、主角处境和第一处异常，让读者知道故事为什么现在开始。", fallbackChapterText(sceneTitle, "第一章：世界入口", "主角第一次踏入陌生的时代规则，雨声、灯影和旁人的目光一同压来。他发现自己不是旁观者，因为一句无心的话已经让原本的命运偏离。章末，门外传来急促脚步，真正的冲突开始逼近。"), "小说正文"),
            StoryboardSection("board_story_2", "第二章：人物关系", "展开主角目标、同盟、阻力和关键人物的利益关系，压出后续冲突。", fallbackChapterText(sceneTitle, "第二章：人物关系", "主角试着在几方势力之间寻找立足点，却发现每个人递来的善意都藏着条件。他与关键人物第一次长谈，对方一句反问让他意识到，自己知道的结局正在变成别人手中的筹码。"), "小说正文"),
            StoryboardSection("board_story_3", "第三章：第一次选择", "安排主角主动改变局面，付出代价或暴露弱点，让故事脱离静态设定。", fallbackChapterText(sceneTitle, "第三章：第一次选择", "面对即将发生的旧事，主角终于没有按原来的故事沉默。他救下一个本该被舍弃的人，也因此暴露了自己不该知道的秘密。夜色落下时，他明白选择已经有了代价。"), "小说正文"),
            StoryboardSection("board_story_4", "第四章：危机反扑", "让旧规则、对手或命运反扑，推动主角发现更深层秘密。", fallbackChapterText(sceneTitle, "第四章：危机反扑", "旧秩序开始追问他的来历，曾经的同盟也被迫站到另一侧。主角在追逐和试探中找到一页残缺线索，才知道自己改动的并非一人命运，而是整段故事的根。"), "小说正文"),
            StoryboardSection("board_story_5", "第五章：余味结尾", "完成本篇小闭环，同时留下下一篇可继续展开的人物承诺或悬念。", fallbackChapterText(sceneTitle, "第五章：余味结尾", "主角完成了本篇最初的承诺，却也亲眼看见新的因果浮出水面。灯火照在他手里的信物上，旧故事暂时合上，下一卷却已经在风中翻开第一页。"), "小说正文"),
        )
        CreationOutputKind.CHARACTER_STORY -> listOf(
            StoryboardSection("board_character_1", "角色身份卡", "明确主角在 $sceneTitle 中的身份、年龄感、服饰方向、气质和与原著人物的距离。", "设定项：身份 / 气质 / 服饰 / 信物", "角色设定"),
            StoryboardSection("board_character_2", "关系位置卡", "梳理主角与原著关键人物的亲疏、误会、旧缘或利益冲突。", "设定项：同盟 / 对照 / 隐秘关系", "关系设定"),
            StoryboardSection("board_character_3", "第一幕剧情", "用一场小冲突让主角进入原著世界，并展示他和旧命运的第一次摩擦。", "剧情作用：入局与亮相", "故事片段"),
            StoryboardSection("board_character_4", "后续扩展方向", "给出可继续做漫画、短视频或小说连载的三个延展钩子。", "扩展项：支线 / 反转 / 下一集", "扩展建议"),
        )
    }
}

private fun fallbackChapterText(
    sceneTitle: String,
    title: String,
    body: String,
): String {
    return "$title\n\n在${sceneTitle}的世界里，$body\n\n这不是一段提纲，而是可以继续阅读的章节正文。"
}
