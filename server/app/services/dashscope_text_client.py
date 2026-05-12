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
    ) -> dict[str, str]:
        fallback = _fallback_story_draft(
            scene_title=scene_title,
            project_title=project_title,
            theme_line=theme_line,
            style_id=style_id,
        )
        system_prompt = (
            "你是映人生的叙事编导。请只返回 JSON，不要输出代码块。"
            "JSON 结构必须包含 title, opening, body, closing 四个字段。"
        )
        user_prompt = (
            f"场景：{scene_title}\n"
            f"项目标题：{project_title}\n"
            f"主题：{theme_line}\n"
            f"风格：{style_id}\n"
            "请生成一版适合历史架空、名著融合、原创小说或小说改编的创作草稿。"
            "需要兼顾小说正文、漫画分镜和短视频脚本的后续扩展。"
        )
        data = self.provider.chat_json(system_prompt, user_prompt, fallback=fallback)
        if not isinstance(data, dict):
            data = fallback
        return {
            "title": str(data.get("title", fallback["title"])),
            "opening": str(data.get("opening", fallback["opening"])),
            "body": str(data.get("body", fallback["body"])),
            "closing": str(data.get("closing", fallback["closing"])),
        }

    def generate_storyboard(
        self,
        scene_title: str,
        story_draft: dict[str, str],
    ) -> list[dict[str, Any]]:
        fallback = _fallback_storyboard(scene_title=scene_title, story_draft=story_draft)
        system_prompt = (
            "你是映人生的分镜编导。请只返回 JSON 数组，不要输出代码块。"
            "数组每项必须包含 title, summary, subtitle_line, duration_label 四个字段。"
            "请严格返回 3 段。"
        )
        user_prompt = (
            f"场景：{scene_title}\n"
            f"标题：{story_draft['title']}\n"
            f"开场：{story_draft['opening']}\n"
            f"主体：{story_draft['body']}\n"
            f"结尾：{story_draft['closing']}\n"
            "请生成三段式故事板，兼容连环漫画与短视频。每段要包含画面、旁白或字幕重点。"
        )
        data = self.provider.chat_json(system_prompt, user_prompt, fallback=fallback)
        if not isinstance(data, list):
            data = fallback
        normalized = []
        for index, item in enumerate(data[:3]):
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
) -> dict[str, str]:
    style_label = {
        "style_classic": "古典章回",
        "style_cinematic": "影视短剧",
        "style_comic": "连环漫画",
        "style_webnovel": "网文爽感",
    }.get(style_id, style_id)
    return {
        "title": f"{scene_title} | {project_title}",
        "opening": f"主角被放入“{scene_title}”的世界后，先以一个强身份钩子进入故事。",
        "body": f"根据“{theme_line}”，系统将主角愿望、时代规则、原著人物关系和第一场冲突整理成 {style_label} 方向的创作主线。",
        "closing": "这一版会继续拆成角色定妆提示、漫画格画面和短视频镜头，等待接入真实图片/视频模型。",
    }


def _fallback_storyboard(scene_title: str, story_draft: dict[str, str]) -> list[dict[str, Any]]:
    _ = story_draft
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
