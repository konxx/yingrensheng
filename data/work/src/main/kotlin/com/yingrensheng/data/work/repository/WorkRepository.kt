package com.yingrensheng.data.work.repository

import com.google.gson.reflect.TypeToken
import com.yingrensheng.core.model.work.Work
import com.yingrensheng.core.network.NetworkApiResponse
import com.yingrensheng.core.network.SimpleApiClient
import com.yingrensheng.core.network.YrsApiConfig
import com.yingrensheng.core.network.requireData
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

interface WorkRepository {
    fun getWorks(): List<Work>
}

object WorkRepositoryProvider {
    @Volatile
    var current: WorkRepository = NetworkWorkRepository.fallbackAware()
}

class FakeWorkRepository : WorkRepository {
    override fun getWorks(): List<Work> {
        return listOf(
            Work(
                workId = "work_001",
                projectId = "project_travel",
                title = "大理五月风",
                sceneLabel = "旅行纪念",
                durationLabel = "00:48",
                statusLabel = "已完成",
                updatedAt = Instant.now().minusSeconds(7200),
                coverUrl = "http://10.0.2.2:3000/storage/previews/project_seed_001.jpg",
                videoUrl = "http://10.0.2.2:3000/storage/previews/project_seed_001.gif",
            ),
            Work(
                workId = "work_002",
                projectId = "project_memory",
                title = "给爸爸的退休片",
                sceneLabel = "人生回忆",
                durationLabel = "01:26",
                statusLabel = "渲染中",
                updatedAt = Instant.now().minusSeconds(14400),
                coverUrl = "",
                videoUrl = "",
            ),
        )
    }
}

class NetworkWorkRepository(
    private val apiClient: SimpleApiClient,
    private val fallback: WorkRepository,
) : WorkRepository {
    override fun getWorks(): List<Work> {
        return runBlocking(Dispatchers.IO) {
            runCatching {
                val type = object : TypeToken<NetworkApiResponse<List<WorkPayload>>>() {}.type
                val envelope: NetworkApiResponse<List<WorkPayload>> = apiClient.get("/works", type)
                envelope.requireData().map {
                    Work(
                        workId = it.workId,
                        projectId = it.projectId,
                        title = it.title,
                        sceneLabel = it.sceneLabel,
                        durationLabel = it.durationLabel,
                        statusLabel = it.statusLabel,
                        updatedAt = runCatching { Instant.parse(it.updatedAt) }.getOrDefault(Instant.now()),
                        coverUrl = it.coverUrl,
                        videoUrl = it.videoUrl,
                    )
                }
            }.getOrElse { fallback.getWorks() }
        }
    }

    companion object {
        fun fallbackAware(): WorkRepository {
            val fake = FakeWorkRepository()
            return NetworkWorkRepository(
                apiClient = SimpleApiClient(YrsApiConfig.DefaultBaseUrl),
                fallback = fake,
            )
        }
    }
}

private data class WorkPayload(
    val workId: String,
    val projectId: String,
    val title: String,
    val sceneLabel: String,
    val durationLabel: String,
    val statusLabel: String,
    val updatedAt: String,
    val coverUrl: String,
    val videoUrl: String,
)
