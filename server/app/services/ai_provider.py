import json
import re
from http import HTTPStatus
from typing import Any

from app.core.settings import settings


class DashScopeAiProvider:
    """DashScope SDK provider for Bailian text, image, video, and TTS models."""

    def __init__(self) -> None:
        self.api_key = settings.dashscope_api_key or settings.ai_text_api_key
        self.image_api_key = settings.ai_image_api_key or self.api_key
        self.video_api_key = settings.ai_video_api_key or self.api_key
        self.tts_api_key = settings.ai_tts_api_key or self.api_key
        self.text_model = settings.ai_text_model
        self.image_model = settings.ai_image_model
        self.video_model = settings.ai_video_model
        self.tts_model = settings.ai_tts_model
        self.base_http_api_url = settings.dashscope_base_http_api_url.rstrip("/")

    @property
    def has_text_credentials(self) -> bool:
        return bool(self.api_key and self.text_model)

    def status(self) -> dict[str, Any]:
        return {
            "provider": "dashscope",
            "mode": "dashscope_python_sdk",
            "openaiCompatible": False,
            "baseHttpApiUrl": self.base_http_api_url,
            "credentials": {
                "dashscopeApiKeyConfigured": bool(self.api_key),
                "imageApiKeyConfigured": bool(self.image_api_key),
                "videoApiKeyConfigured": bool(self.video_api_key),
                "ttsApiKeyConfigured": bool(self.tts_api_key),
            },
            "models": {
                "text": self.text_model,
                "image": self.image_model,
                "video": self.video_model,
                "tts": self.tts_model,
            },
            "sdk": {
                "generation": _sdk_available(_dashscope_generation),
                "multimodalConversation": _sdk_available(_dashscope_multimodal),
                "videoSynthesis": _sdk_available(_dashscope_video),
            },
        }

    def chat_json(self, system_prompt: str, user_prompt: str, fallback: Any) -> Any:
        if not self.has_text_credentials or not settings.ai_text_sync_enabled:
            return fallback
        if _requires_multimodal_text(self.text_model):
            return self._multimodal_chat_json(system_prompt, user_prompt)
        try:
            dashscope, generation_cls = _dashscope_generation()
        except ImportError:
            return fallback

        dashscope.base_http_api_url = self.base_http_api_url
        response = generation_cls.call(
            api_key=self.api_key,
            model=self.text_model,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            result_format="message",
            temperature=0.75,
        )
        if getattr(response, "status_code", None) != HTTPStatus.OK:
            message = getattr(response, "message", "DashScope text generation failed")
            code = getattr(response, "code", "UNKNOWN")
            raise RuntimeError(f"DashScope text generation failed: {code} {message}")

        content = _extract_text_content(response)
        return _extract_json(content)

    def _multimodal_chat_json(self, system_prompt: str, user_prompt: str) -> Any:
        try:
            dashscope, conversation_cls = _dashscope_multimodal()
        except ImportError as exc:
            raise RuntimeError("DashScope SDK is not installed") from exc

        dashscope.base_http_api_url = self.base_http_api_url
        response = conversation_cls.call(
            api_key=self.api_key,
            model=self.text_model,
            messages=[
                {
                    "role": "user",
                    "content": [
                        {
                            "text": (
                                f"{system_prompt}\n\n"
                                f"用户输入：\n{user_prompt}"
                            ),
                        },
                    ],
                },
            ],
        )
        if getattr(response, "status_code", None) != HTTPStatus.OK:
            message = getattr(response, "message", "DashScope multimodal generation failed")
            code = getattr(response, "code", "UNKNOWN")
            raise RuntimeError(f"DashScope multimodal generation failed: {code} {message}")

        content = _extract_multimodal_text_content(response)
        return _extract_json(content)

    def generate_image(
        self,
        prompt: str,
        image_inputs: list[str] | None = None,
        size: str = "1024*1536",
    ) -> dict[str, Any]:
        if not self.image_api_key:
            return {"provider": "dashscope", "status": "pending_credentials", "prompt": prompt}
        try:
            dashscope, conversation_cls = _dashscope_multimodal()
        except ImportError:
            return {"provider": "dashscope", "status": "sdk_missing", "prompt": prompt}

        dashscope.base_http_api_url = self.base_http_api_url
        content = [{"image": item} for item in (image_inputs or [])] + [{"text": prompt}]
        response = conversation_cls.call(
            api_key=self.image_api_key,
            model=self.image_model,
            messages=[{"role": "user", "content": content}],
            stream=False,
            n=1,
            watermark=False,
            negative_prompt="低分辨率、错误、最差质量、低质量、残缺、多余的手指、比例不良",
            prompt_extend=True,
            size=size,
        )
        if getattr(response, "status_code", None) != HTTPStatus.OK:
            return _failure_payload(response, prompt)

        urls = _extract_image_urls(response)
        return {
            "provider": "dashscope",
            "status": "succeeded",
            "model": self.image_model,
            "image_urls": urls,
        }

    def generate_video(
        self,
        prompt: str,
        first_frame_url: str,
        duration: int = 10,
        resolution: str = "720P",
    ) -> dict[str, Any]:
        if not self.video_api_key:
            return {"provider": "dashscope", "status": "pending_credentials", "prompt": prompt}
        try:
            dashscope, video_cls = _dashscope_video()
        except ImportError:
            return {"provider": "dashscope", "status": "sdk_missing", "prompt": prompt}

        dashscope.base_http_api_url = self.base_http_api_url
        response = video_cls.async_call(
            api_key=self.video_api_key,
            model=self.video_model,
            media=[{"type": "first_frame", "url": first_frame_url}],
            resolution=resolution,
            duration=duration,
            watermark=True,
            prompt=prompt,
        )
        if getattr(response, "status_code", None) != HTTPStatus.OK:
            return _failure_payload(response, prompt)
        output = getattr(response, "output", None)
        return {
            "provider": "dashscope",
            "status": "submitted",
            "model": self.video_model,
            "task_id": _output_value(output, "task_id"),
            "task_status": _output_value(output, "task_status"),
        }

    def fetch_video(self, task_id: str) -> dict[str, Any]:
        if not self.video_api_key:
            return {"provider": "dashscope", "status": "pending_credentials", "task_id": task_id}
        try:
            dashscope, video_cls = _dashscope_video()
        except ImportError:
            return {"provider": "dashscope", "status": "sdk_missing", "task_id": task_id}

        dashscope.base_http_api_url = self.base_http_api_url
        response = video_cls.fetch(task=task_id, api_key=self.video_api_key)
        if getattr(response, "status_code", None) != HTTPStatus.OK:
            return _failure_payload(response, task_id)
        output = getattr(response, "output", None)
        return {
            "provider": "dashscope",
            "status": "fetched",
            "model": self.video_model,
            "task_id": task_id,
            "task_status": _output_value(output, "task_status"),
            "video_url": _output_value(output, "video_url"),
            "submit_time": _output_value(output, "submit_time"),
            "scheduled_time": _output_value(output, "scheduled_time"),
            "end_time": _output_value(output, "end_time"),
        }

    def synthesize_speech(
        self,
        text: str,
        voice: str = "Cherry",
        language_type: str = "Chinese",
    ) -> dict[str, Any]:
        if not self.tts_api_key:
            return {"provider": "dashscope", "status": "pending_credentials", "text": text}
        try:
            dashscope, conversation_cls = _dashscope_multimodal()
        except ImportError:
            return {"provider": "dashscope", "status": "sdk_missing", "text": text}

        dashscope.base_http_api_url = self.base_http_api_url
        response = conversation_cls.call(
            api_key=self.tts_api_key,
            model=self.tts_model,
            text=text,
            voice=voice,
            language_type=language_type,
        )
        if getattr(response, "status_code", None) != HTTPStatus.OK:
            return _failure_payload(response, text)
        audio = response.output.get("audio") if isinstance(response.output, dict) else getattr(response.output, "audio", None)
        return {
            "provider": "dashscope",
            "status": "succeeded",
            "model": self.tts_model,
            "audio_url": audio.get("url") if isinstance(audio, dict) else getattr(audio, "url", None),
            "audio_id": audio.get("id") if isinstance(audio, dict) else getattr(audio, "id", None),
        }

    def generate_image_plan(self, character_prompt: str) -> dict[str, str]:
        return {
            "provider": "dashscope",
            "status": "ready",
            "model": self.image_model,
            "prompt": character_prompt,
        }

    def generate_video_plan(self, storyboard_prompt: str) -> dict[str, str]:
        return {
            "provider": "dashscope",
            "status": "ready",
            "model": self.video_model,
            "prompt": storyboard_prompt,
        }


def _dashscope_generation():
    import dashscope
    from dashscope import Generation

    return dashscope, Generation


def _dashscope_multimodal():
    import dashscope
    from dashscope import MultiModalConversation

    return dashscope, MultiModalConversation


def _dashscope_video():
    import dashscope
    from dashscope import VideoSynthesis

    return dashscope, VideoSynthesis


def _sdk_available(loader: Any) -> bool:
    try:
        loader()
    except ImportError:
        return False
    return True


def _extract_text_content(response: Any) -> str:
    output = response.output
    choices = output.get("choices") if isinstance(output, dict) else getattr(output, "choices", None)
    if choices:
        first = choices[0]
        message = first.get("message") if isinstance(first, dict) else getattr(first, "message", None)
        if isinstance(message, dict):
            return str(message.get("content", ""))
        return str(getattr(message, "content", ""))
    text = output.get("text") if isinstance(output, dict) else getattr(output, "text", None)
    return str(text or "")


def _extract_multimodal_text_content(response: Any) -> str:
    output = response.output
    choices = output.get("choices") if isinstance(output, dict) else getattr(output, "choices", None)
    if not choices:
        return ""
    first = choices[0]
    message = first.get("message") if isinstance(first, dict) else getattr(first, "message", None)
    content = message.get("content") if isinstance(message, dict) else getattr(message, "content", None)
    if isinstance(content, list):
        parts: list[str] = []
        for item in content:
            if isinstance(item, dict) and item.get("text"):
                parts.append(str(item["text"]))
            elif getattr(item, "text", None):
                parts.append(str(item.text))
        return "\n".join(parts)
    return str(content or "")


def _extract_image_urls(response: Any) -> list[str]:
    output = getattr(response, "output", None)
    choices = output.get("choices") if isinstance(output, dict) else getattr(output, "choices", None)
    if not choices:
        return []
    first = choices[0]
    message = first.get("message") if isinstance(first, dict) else getattr(first, "message", None)
    content = message.get("content") if isinstance(message, dict) else getattr(message, "content", None)
    urls: list[str] = []
    for item in content or []:
        image_url = item.get("image") if isinstance(item, dict) else getattr(item, "image", None)
        if image_url:
            urls.append(str(image_url))
    return urls


def _output_value(output: Any, key: str) -> Any:
    if isinstance(output, dict):
        return output.get(key)
    return getattr(output, key, None)


def _extract_json(text: str) -> Any:
    array_match = re.search(r"\[[\s\S]*\]", text)
    object_match = re.search(r"\{[\s\S]*\}", text)
    match = array_match or object_match
    if not match:
        raise ValueError(f"Model response did not contain JSON: {text}")
    return json.loads(match.group(0))


def _failure_payload(response: Any, prompt: str) -> dict[str, Any]:
    return {
        "provider": "dashscope",
        "status": "failed",
        "code": getattr(response, "code", "UNKNOWN"),
        "message": getattr(response, "message", ""),
        "prompt": prompt,
    }


def _requires_multimodal_text(model: str) -> bool:
    normalized = model.lower()
    return (
        normalized.startswith("qwen3.6")
        or normalized.startswith("qwen3.5")
        or normalized.startswith("qwen3-vl")
        or normalized.startswith("qwen-vl")
    )
