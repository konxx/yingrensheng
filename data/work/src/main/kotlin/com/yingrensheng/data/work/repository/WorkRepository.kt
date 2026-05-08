package com.yingrensheng.data.work.repository

import com.yingrensheng.core.model.work.Work
import java.time.Instant

interface WorkRepository {
    fun getWorks(): List<Work>
}

object WorkRepositoryProvider {
    @Volatile
    var current: WorkRepository = FakeWorkRepository()
}

class FakeWorkRepository : WorkRepository {
    override fun getWorks(): List<Work> {
        return listOf(
            Work(
                workId = "work_001",
                title = "大理五月风",
                sceneLabel = "旅行纪念",
                durationLabel = "00:48",
                statusLabel = "已完成",
                updatedAt = Instant.now().minusSeconds(7200),
            ),
            Work(
                workId = "work_002",
                title = "给爸爸的退休片",
                sceneLabel = "人生回忆",
                durationLabel = "01:26",
                statusLabel = "渲染中",
                updatedAt = Instant.now().minusSeconds(14400),
            ),
        )
    }
}
