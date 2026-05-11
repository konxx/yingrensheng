import json
import re
from typing import Any

import httpx

from app.core.settings import settings


class ArkTextClient:
    def __init__(self) -> None:
        self.base_url = settings.ark_text_base_url.rstrip("/")
        self.api_key = settings.ark_text_api_key
        self.model = settings.ark_text_model

    def _chat(self, system_prompt: str, user_prompt: str) -> str:
        if not self.api_key:
            raise RuntimeError("ARK_TEXT_API_KEY is not configured")
        response = httpx.post(
            f"{self.base_url}/chat/completions",
            headers={
                "Authorization": f"Bearer {self.api_key}",
                "Content-Type": "application/json",
            },
            json={
                "model": self.model,
                "messages": [
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt},
                ],
                "temperature": 0.7,
            },
            timeout=60.0,
        )
        response.raise_for_status()
        data = response.json()
        return data["choices"][0]["message"]["content"]

    def generate_story_draft(
        self,
        scene_title: str,
        project_title: str,
        theme_line: str,
        style_id: str,
    ) -> dict[str, str]:
        system_prompt = (
            "你是映人生的叙事编导。请只返回 JSON，不要输出代码块。"
            "JSON 结构必须包含 title, opening, body, closing 四个字段。"
        )
        user_prompt = (
            f"场景：{scene_title}\n"
            f"项目标题：{project_title}\n"
            f"主题：{theme_line}\n"
            f"风格：{style_id}\n"
            "请生成一版适合短视频纪念片的故事草稿，语气真诚、自然、适合普通用户分享。"
        )
        content = self._chat(system_prompt, user_prompt)
        return _extract_json_object(content)

    def generate_storyboard(
        self,
        scene_title: str,
        story_draft: dict[str, str],
    ) -> list[dict[str, Any]]:
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
            "请生成三段式故事板，分别适合开场、高光、收束。"
        )
        content = self._chat(system_prompt, user_prompt)
        data = _extract_json_array(content)
        normalized = []
        for index, item in enumerate(data):
            normalized.append(
                {
                    "section_id": f"section_gen_{index + 1:03d}",
                    "title": item["title"],
                    "summary": item["summary"],
                    "subtitle_line": item["subtitle_line"],
                    "duration_label": item["duration_label"],
                },
            )
        return normalized


def _extract_json_object(text: str) -> dict[str, str]:
    match = re.search(r"\{[\s\S]*\}", text)
    if not match:
        raise ValueError(f"Model response did not contain JSON object: {text}")
    return json.loads(match.group(0))


def _extract_json_array(text: str) -> list[dict[str, Any]]:
    match = re.search(r"\[[\s\S]*\]", text)
    if not match:
        raise ValueError(f"Model response did not contain JSON array: {text}")
    return json.loads(match.group(0))

