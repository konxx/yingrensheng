package com.yingrensheng.core.model.creation

import com.yingrensheng.core.model.material.MaterialItem
import com.yingrensheng.core.model.project.CreationMode

data class SceneTemplate(
    val sceneId: String,
    val title: String,
    val subtitle: String,
    val recommendedDurationLabel: String,
    val estimatedTimeLabel: String,
)

data class InterviewPrompt(
    val promptId: String,
    val title: String,
    val helper: String,
)

data class NarrativeStyle(
    val styleId: String,
    val title: String,
    val description: String,
)

data class StoryDraft(
    val title: String,
    val opening: String,
    val body: String,
    val closing: String,
)

data class StoryboardSection(
    val sectionId: String,
    val title: String,
    val summary: String,
    val subtitleLine: String,
    val durationLabel: String,
)

data class PreviewAsset(
    val title: String,
    val subtitleSummary: String,
    val musicLabel: String,
    val coverCaption: String,
    val coverUrl: String = "",
    val videoUrl: String = "",
)

data class RenderTask(
    val taskId: String,
    val stage: String,
    val progress: Int,
    val estimatedRemainingSeconds: Int,
)

data class ExportPlan(
    val planId: String,
    val title: String,
    val priceLabel: String,
    val benefits: List<String>,
)

enum class CreationOutputKind {
    CHARACTER_STORY,
    STORY_TEXT,
    COMIC_STORYBOARD,
    SHORT_VIDEO,
}

data class CreationSession(
    val mode: CreationMode? = null,
    val selectedScene: SceneTemplate? = null,
    val materials: List<MaterialItem> = emptyList(),
    val themeLine: String = "",
    val interviewAnswers: Map<String, String> = emptyMap(),
    val selectedStyle: NarrativeStyle? = null,
    val storyDraft: StoryDraft? = null,
    val storyboard: List<StoryboardSection> = emptyList(),
    val previewAsset: PreviewAsset? = null,
    val renderTask: RenderTask? = null,
    val selectedExportPlan: ExportPlan? = null,
    val currentProjectId: String? = null,
)

fun CreationSession.outputKind(): CreationOutputKind {
    val sceneId = selectedScene?.sceneId.orEmpty()
    return when {
        sceneId == "scene_media_short_video" -> CreationOutputKind.SHORT_VIDEO
        sceneId == "scene_media_comic" -> CreationOutputKind.COMIC_STORYBOARD
        sceneId.startsWith("scene_outline") || mode == CreationMode.OUTLINE_STORY -> CreationOutputKind.STORY_TEXT
        sceneId.startsWith("scene_character") || mode == CreationMode.CHARACTER_TIME_TRAVEL -> CreationOutputKind.CHARACTER_STORY
        else -> CreationOutputKind.STORY_TEXT
    }
}

fun CreationSession.requiresVideoPreview(): Boolean = outputKind() == CreationOutputKind.SHORT_VIDEO
