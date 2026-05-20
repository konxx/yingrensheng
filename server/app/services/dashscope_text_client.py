from typing import Any

from app.services.ai_provider import DashScopeAiProvider


class DashScopeTextClient:
    def __init__(self) -> None:
        self.provider = DashScopeAiProvider()

    def generate_story_draft(
        self,
        scene_title: str,
        project_title: str,
        theme_line: str,
        style_id: str,
        scene_id: str = "",
    ) -> dict[str, str]:
        fallback = _fallback_story_draft(
            scene_title=scene_title,
            project_title=project_title,
            theme_line=theme_line,
            style_id=style_id,
            scene_id=scene_id,
        )
        flow_goal = _flow_goal(scene_id=scene_id)
        system_prompt = (
            "你是映人生的叙事编导。请只返回 JSON，不要输出代码块。"
            "JSON 结构必须包含 title, opening, body, closing 四个字段。"
        )
        user_prompt = (
            f"创作流程：{flow_goal}\n"
            f"场景：{scene_title}\n"
            f"项目标题：{project_title}\n"
            f"主题：{theme_line}\n"
            f"风格：{style_id}\n"
            "请生成一版第一版创作稿，必须贴合当前流程的最终交付物。"
            "如果是连环漫画，正文要服务于分格画面和对白；如果是短视频，正文要服务于镜头、旁白和首帧。"
            "如果是小说或角色故事，不要写成视频成片方案。"
        )
        try:
            data = self.provider.chat_json(system_prompt, user_prompt, fallback=fallback)
        except Exception:
            data = fallback
        if not isinstance(data, dict):
            data = fallback
        body = str(data.get("body", fallback["body"]))
        if "AI 导演补齐设定：" in theme_line and "AI 导演补齐设定：" not in body:
            body = f"{body}\n\n已记录的 AI 导演补齐设定：\n{_extract_director_notes(theme_line)}"
        return {
            "title": str(data.get("title", fallback["title"])),
            "opening": str(data.get("opening", fallback["opening"])),
            "body": body,
            "closing": str(data.get("closing", fallback["closing"])),
        }

    def generate_storyboard(
        self,
        scene_title: str,
        story_draft: dict[str, str],
        scene_id: str = "",
    ) -> list[dict[str, Any]]:
        fallback = _fallback_storyboard(scene_title=scene_title, story_draft=story_draft, scene_id=scene_id)
        board_count = _storyboard_count(scene_id=scene_id)
        board_goal = _storyboard_goal(scene_id=scene_id)
        system_prompt = (
            "你是映人生的分镜编导。请只返回 JSON 数组，不要输出代码块。"
            "数组每项必须包含 title, summary, subtitle_line, duration_label 四个字段。"
            f"请严格返回 {board_count} 项。"
        )
        user_prompt = (
            f"分镜目标：{board_goal}\n"
            f"场景：{scene_title}\n"
            f"标题：{story_draft['title']}\n"
            f"开场：{story_draft['opening']}\n"
            f"主体：{story_draft['body']}\n"
            f"结尾：{story_draft['closing']}\n"
            "请按当前目标生成，不要把连环漫画写成视频镜头，也不要把小说结构写成视频导出方案。"
        )
        try:
            data = self.provider.chat_json(system_prompt, user_prompt, fallback=fallback)
        except Exception:
            data = fallback
        if not isinstance(data, list):
            data = fallback
        items = data[:board_count]
        if len(items) < board_count:
            items = items + fallback[len(items):board_count]
        normalized = []
        for index, item in enumerate(items):
            if not isinstance(item, dict):
                item = fallback[min(index, len(fallback) - 1)]
            normalized.append(
                {
                    "section_id": f"section_gen_{index + 1:03d}",
                    "title": item.get("title", fallback[index]["title"]),
                    "summary": item.get("summary", fallback[index]["summary"]),
                    "subtitle_line": item.get("subtitle_line", item.get("subtitleLine", fallback[index]["subtitle_line"])),
                    "duration_label": item.get("duration_label", item.get("durationLabel", fallback[index]["duration_label"])),
                },
            )
        return normalized


def _fallback_story_draft(
    scene_title: str,
    project_title: str,
    theme_line: str,
    style_id: str,
    scene_id: str = "",
) -> dict[str, str]:
    style_label = {
        "style_classic": "古典章回",
        "style_cinematic": "影视短剧",
        "style_comic": "连环漫画",
        "style_webnovel": "网文爽感",
    }.get(style_id, style_id)
    if scene_id == "scene_media_comic":
        closing = "下一步只拆连环漫画分格，输出画面、人物动作、对白、字幕和页内节奏，不进入视频预览。"
        body = f"根据“{theme_line}”，系统将人物、地点、关键动作和对白爆点整理成 {style_label} 方向的漫画脚本主线。"
    elif scene_id == "scene_media_short_video":
        closing = "下一步会拆短视频镜头，再调用 DashScope 首帧图和图生视频预览。"
        body = f"根据“{theme_line}”，系统将前 5 秒钩子、镜头推进、旁白和字幕节奏整理成 {style_label} 方向的短视频脚本。"
    elif scene_id == "scene_outline_history":
        closing = "下一步整理章节梗概、人物关系和历史融合点，交付小说创作包。"
        body = f"根据“{theme_line}”，系统将朝代质感、名著参照、人物欲望和核心冲突整理成 {style_label} 方向的历史融合小说。"
    elif scene_id == "scene_outline_original":
        closing = "这一版会保留原创世界观、人物欲望和结尾余味，直接进入小说成果确认。"
        body = f"根据“{theme_line}”，系统将主角欲望、世界规则、阻力和结局情绪整理成 {style_label} 方向的原创故事。"
    elif scene_id == "scene_character_honglou":
        closing = "下一步整理红楼关系网、人物设定卡和第一段剧情，不进入视频预览。"
        body = f"根据“{theme_line}”，系统将人物气质、贾府位置、宝黛钗关系和第一场冲突整理成 {style_label} 方向的角色故事。"
    else:
        closing = "下一步整理角色身份、定妆提示和取经路第一段剧情，不进入视频预览。"
        body = f"根据“{theme_line}”，系统将人物气质、身份钩子、原著人物关系和第一场冲突整理成 {style_label} 方向的角色故事。"

    return {
        "title": f"{scene_title} | {project_title}",
        "opening": f"主角被放入“{scene_title}”的世界后，先以一个强身份钩子进入故事。",
        "body": body,
        "closing": closing,
    }


def _extract_director_notes(theme_line: str) -> str:
    marker = "AI 导演补齐设定："
    _, _, tail = theme_line.partition(marker)
    if not tail:
        return ""
    notes, _, _ = tail.partition("\n用户素材：")
    return notes.strip()


def _fallback_storyboard(scene_title: str, story_draft: dict[str, str], scene_id: str = "") -> list[dict[str, Any]]:
    _ = story_draft
    if scene_id == "scene_media_comic":
        return [
            {
                "title": "第 1 格：入画钩子",
                "summary": f"远景建立 {scene_title} 的时代和地点，主角从原小说关键场景中被推到画面前景。",
                "subtitle_line": "旁白框：旧故事翻到这一页时，他忽然成了画中人。",
                "duration_label": "第 1 页 / 上排大格",
            },
            {
                "title": "第 2 格：人物亮相",
                "summary": "中近景刻画主角服饰、神态和手中关键物件，保留用户输入里的气质标签。",
                "subtitle_line": "对白：我记得这里，可书里没有我。",
                "duration_label": "第 1 页 / 右上格",
            },
            {
                "title": "第 3 格：关系碰撞",
                "summary": "原著人物或对手入画，两人视线相撞，背景用门廊、街市或庭院压出空间层次。",
                "subtitle_line": "对白：你是谁，怎会知道这件事？",
                "duration_label": "第 1 页 / 中排左格",
            },
            {
                "title": "第 4 格：关键动作",
                "summary": "主角做出第一次改变剧情的动作，画面突出手势、衣袖、道具和围观反应。",
                "subtitle_line": "拟声：啪。",
                "duration_label": "第 1 页 / 中排右格",
            },
            {
                "title": "第 5 格：反应定格",
                "summary": "切到对方表情和环境细节，让读者看见这一步已经改变原本命运。",
                "subtitle_line": "旁白框：命数像被墨滴晕开。",
                "duration_label": "第 2 页 / 上排左格",
            },
            {
                "title": "第 6 格：冲突升级",
                "summary": "用斜构图表现追问、误会或追逐，人物身体方向形成明确动线。",
                "subtitle_line": "对白：既然你知前因，就该知道后果。",
                "duration_label": "第 2 页 / 上排右格",
            },
            {
                "title": "第 7 格：情绪反转",
                "summary": "主角发现真正代价，画面压暗，只留脸部高光或关键物件亮色。",
                "subtitle_line": "旁白框：他终于明白，改写不是逃离。",
                "duration_label": "第 2 页 / 中排整格",
            },
            {
                "title": "第 8 格：尾页悬念",
                "summary": "最后一格留出下一回钩子，远处人物、信物或门后阴影引出续篇。",
                "subtitle_line": "字幕：下一页，旧命簿开始回看他。",
                "duration_label": "第 2 页 / 结尾大格",
            },
        ]
    if scene_id == "scene_media_short_video":
        return [
            {
                "title": "0-5 秒：强钩子",
                "summary": "用反常识台词、命运危机或身份错位直接开场，首帧必须有人物和强冲突。",
                "subtitle_line": "字幕：他刚进书里，就改掉了主角的命。",
                "duration_label": "0-5s",
            },
            {
                "title": "5-18 秒：身份交代",
                "summary": "镜头从道具推到人物脸部，交代主角身份、地点和与原故事的关系。",
                "subtitle_line": "旁白：这里所有人都按旧书活着，只有他记得结局。",
                "duration_label": "5-18s",
            },
            {
                "title": "18-38 秒：冲突升级",
                "summary": "用三到四个快切镜头呈现阻拦、追问、误会或打斗，字幕同步推进剧情。",
                "subtitle_line": "字幕：他救下的人，偏偏是原书里最不该活的人。",
                "duration_label": "18-38s",
            },
            {
                "title": "38-60 秒：反转收束",
                "summary": "镜头慢下来，留一个代价、秘密或下集承诺，适合导出为短视频预览。",
                "subtitle_line": "字幕：可他不知道，命运也在改写他。",
                "duration_label": "38-60s",
            },
        ]
    if scene_id.startswith("scene_outline"):
        return [
            {
                "title": "第一章：世界入口",
                "summary": f"建立 {scene_title} 的时代规则、主角处境和第一处异常，让读者知道故事为什么现在开始。",
                "subtitle_line": "章节作用：开篇钩子与世界观落点",
                "duration_label": "章节梗概",
            },
            {
                "title": "第二章：人物关系",
                "summary": "展开主角目标、同盟、阻力和关键人物的利益关系，压出后续冲突。",
                "subtitle_line": "章节作用：人物小传与关系网",
                "duration_label": "章节梗概",
            },
            {
                "title": "第三章：第一次选择",
                "summary": "安排主角主动改变局面，付出代价或暴露弱点，让故事脱离静态设定。",
                "subtitle_line": "章节作用：行动线启动",
                "duration_label": "章节梗概",
            },
            {
                "title": "第四章：危机反扑",
                "summary": "让旧规则、对手或命运反扑，推动主角发现更深层秘密。",
                "subtitle_line": "章节作用：中段升级",
                "duration_label": "章节梗概",
            },
            {
                "title": "第五章：余味结尾",
                "summary": "完成本篇小闭环，同时留下下一篇可继续展开的人物承诺或悬念。",
                "subtitle_line": "章节作用：结局余韵与续写钩子",
                "duration_label": "章节梗概",
            },
        ]
    if scene_id.startswith("scene_character"):
        return [
            {
                "title": "角色身份卡",
                "summary": f"明确主角在 {scene_title} 中的身份、年龄感、服饰方向、气质和与原著人物的距离。",
                "subtitle_line": "设定项：身份 / 气质 / 服饰 / 信物",
                "duration_label": "角色设定",
            },
            {
                "title": "关系位置卡",
                "summary": "梳理主角与原著关键人物的亲疏、误会、旧缘或利益冲突。",
                "subtitle_line": "设定项：同盟 / 对照 / 隐秘关系",
                "duration_label": "关系设定",
            },
            {
                "title": "第一幕剧情",
                "summary": "用一场小冲突让主角进入原著世界，并展示他和旧命运的第一次摩擦。",
                "subtitle_line": "剧情作用：入局与亮相",
                "duration_label": "故事片段",
            },
            {
                "title": "后续扩展方向",
                "summary": "给出可继续做漫画、短视频或小说连载的三个延展钩子。",
                "subtitle_line": "扩展项：支线 / 反转 / 下一集",
                "duration_label": "扩展建议",
            },
        ]
    return [
        {
            "title": "身份亮相",
            "summary": f"用一张角色定妆照或开篇场景建立 {scene_title} 的世界入口，交代主角身份和命运变化。",
            "subtitle_line": "字幕：他一睁眼，已站在旧故事的分岔口",
            "duration_label": "00:08",
        },
        {
            "title": "冲突推进",
            "summary": "主角与原著人物或原创对手第一次正面碰撞，画面突出服饰、地点和情绪反差。",
            "subtitle_line": "字幕：旧规矩还在，他却先改了第一步",
            "duration_label": "00:18",
        },
        {
            "title": "悬念收束",
            "summary": "用一个反转、约定或未解谜题收尾，保留下一集继续生成的空间。",
            "subtitle_line": "字幕：下一回，真正的考验才开始",
            "duration_label": "00:10",
        },
    ]


def _flow_goal(scene_id: str) -> str:
    return {
        "scene_character_xiyou": "用户照片架空为西游角色，交付角色故事包",
        "scene_character_honglou": "用户照片进入红楼关系网，交付角色故事包",
        "scene_outline_history": "用户大纲融合历史/名著世界，交付小说创作包",
        "scene_outline_original": "用户灵感扩写原创故事，交付小说创作包",
        "scene_media_comic": "小说改编为连环漫画，交付漫画分镜包",
        "scene_media_short_video": "小说改编为短视频，交付视频预览与导出",
    }.get(scene_id, "通用创作包")


def _storyboard_goal(scene_id: str) -> str:
    return {
        "scene_media_comic": "生成 8 格连环漫画分镜，每格包含画面、人物动作、对白或旁白框、页内位置",
        "scene_media_short_video": "生成 4 段短视频镜头结构，每段包含镜头、旁白、字幕和时长",
        "scene_outline_history": "生成 5 个小说章节梗概，突出历史融合点、人物冲突和结尾余味",
        "scene_outline_original": "生成 5 个原创小说章节梗概，突出世界规则、人物欲望和反转",
        "scene_character_xiyou": "生成角色身份、关系、第一幕剧情和后续扩展，不写成视频镜头",
        "scene_character_honglou": "生成红楼角色身份、关系网、第一幕剧情和后续扩展，不写成视频镜头",
    }.get(scene_id, "生成结构化创作资料")


def _storyboard_count(scene_id: str) -> int:
    if scene_id == "scene_media_comic":
        return 8
    if scene_id == "scene_media_short_video":
        return 4
    if scene_id.startswith("scene_outline"):
        return 5
    if scene_id.startswith("scene_character"):
        return 4
    return 3
