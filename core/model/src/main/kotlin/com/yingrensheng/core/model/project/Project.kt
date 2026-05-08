package com.yingrensheng.core.model.project

import java.time.Instant

enum class CreationMode {
    QUICK_FILM,
    STORY_FILM,
}

enum class ProjectStatus {
    DRAFT,
    GENERATING,
    PREVIEW_READY,
    EXPORTED,
}

data class Project(
    val projectId: String,
    val title: String,
    val sceneType: String,
    val mode: CreationMode,
    val status: ProjectStatus,
    val progress: Int,
    val currentStep: String,
    val materialCount: Int,
    val moodLabel: String,
    val updatedAt: Instant,
)

