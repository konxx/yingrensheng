package com.yingrensheng.core.model.work

import java.time.Instant

data class Work(
    val workId: String,
    val projectId: String,
    val title: String,
    val sceneLabel: String,
    val durationLabel: String,
    val statusLabel: String,
    val updatedAt: Instant,
    val coverUrl: String,
    val videoUrl: String,
)
