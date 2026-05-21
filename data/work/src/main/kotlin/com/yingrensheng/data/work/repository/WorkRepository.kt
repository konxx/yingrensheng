package com.yingrensheng.data.work.repository

import com.google.gson.reflect.TypeToken
import com.yingrensheng.core.model.creation.StoryboardSection
import com.yingrensheng.core.model.work.Work
import com.yingrensheng.core.model.work.WorkAsset
import com.yingrensheng.core.network.NetworkApiResponse
import com.yingrensheng.core.network.SimpleApiClient
import com.yingrensheng.core.network.YrsApiConfig
import com.yingrensheng.core.network.requireData
import java.time.Instant
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

interface WorkRepository {
    fun getWorks(): List<Work>
    fun getWorkAssets(workId: String): List<WorkAsset>
    fun getStoryboard(projectId: String): List<StoryboardSection>
    fun deleteWork(workId: String): Boolean
}

object WorkRepositoryProvider {
    @Volatile
    var current: WorkRepository = NetworkWorkRepository.fallbackAware()
}

class FakeWorkRepository : WorkRepository {
    private val works = mutableListOf(
        Work(
            workId = "work_001",
            projectId = "project_character",
            title = "我在女儿国当取经人",
            sceneLabel = "西游记角色穿越",
            durationLabel = "角色海报 + 00:38",
            statusLabel = "已完成",
            updatedAt = Instant.now().minusSeconds(7200),
            coverUrl = YrsApiConfig.asset("/storage/previews/project_seed_001.jpg"),
            videoUrl = YrsApiConfig.asset("/storage/previews/project_seed_001.gif"),
            outputKind = "SHORT_VIDEO",
        ),
        Work(
            workId = "work_002",
            projectId = "project_novel",
            title = "贾府来客",
            sceneLabel = "融合历史小说",
            durationLabel = "12 格漫画",
            statusLabel = "渲染中",
            updatedAt = Instant.now().minusSeconds(14400),
            coverUrl = "",
            videoUrl = "",
            outputKind = "COMIC_STORYBOARD",
        ),
    )

    override fun getWorks(): List<Work> {
        return works.toList()
    }

    override fun getWorkAssets(workId: String): List<WorkAsset> {
        val work = works.firstOrNull { it.workId == workId } ?: return emptyList()
        return if (work.outputKind == "COMIC_STORYBOARD") {
            sampleComicStoryboard(work.projectId).mapIndexed { index, section ->
                WorkAsset(
                    assetId = "asset_fake_${index + 1}",
                    workId = workId,
                    projectId = work.projectId,
                    outputKind = "COMIC_STORYBOARD",
                    assetType = "COMIC_PANEL",
                    orderIndex = index,
                    title = section.title,
                    summary = section.summary,
                    url = "",
                    textContent = section.subtitleLine,
                )
            }
        } else {
            listOf(
                WorkAsset(
                    assetId = "asset_fake_text",
                    workId = workId,
                    projectId = work.projectId,
                    outputKind = work.outputKind,
                    assetType = "TEXT",
                    orderIndex = 0,
                    title = work.title,
                    summary = "已整理完成的成品包。",
                    url = work.coverUrl,
                    textContent = "这里展示完整作品正文、设定或脚本。",
                ),
            )
        }
    }

    override fun getStoryboard(projectId: String): List<StoryboardSection> {
        return if (projectId == "project_novel") {
            sampleComicStoryboard(projectId)
        } else {
            emptyList()
        }
    }

    override fun deleteWork(workId: String): Boolean {
        return works.removeAll { it.workId == workId }
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
                        coverUrl = YrsApiConfig.assetUrl(it.coverUrl),
                        videoUrl = YrsApiConfig.assetUrl(it.videoUrl),
                        outputKind = it.outputKind,
                    )
                }
            }.getOrElse { fallback.getWorks() }
        }
    }

    override fun getStoryboard(projectId: String): List<StoryboardSection> {
        return runBlocking(Dispatchers.IO) {
            runCatching {
                val type = object : TypeToken<NetworkApiResponse<List<StoryboardPayload>>>() {}.type
                val encodedProjectId = URLEncoder.encode(projectId, StandardCharsets.UTF_8.name())
                val envelope: NetworkApiResponse<List<StoryboardPayload>> = apiClient.get(
                    "/projects/$encodedProjectId/storyboard",
                    type,
                )
                envelope.requireData().sortedBy { it.orderIndex }.map {
                    StoryboardSection(
                        sectionId = it.sectionId,
                        title = it.title,
                        summary = it.summary,
                        subtitleLine = it.subtitleLine,
                        durationLabel = it.durationLabel,
                    )
                }
            }.getOrElse {
                fallback.getStoryboard(projectId)
            }
        }
    }

    override fun getWorkAssets(workId: String): List<WorkAsset> {
        return runBlocking(Dispatchers.IO) {
            runCatching {
                val type = object : TypeToken<NetworkApiResponse<List<WorkAssetPayload>>>() {}.type
                val encodedWorkId = URLEncoder.encode(workId, StandardCharsets.UTF_8.name())
                val envelope: NetworkApiResponse<List<WorkAssetPayload>> = apiClient.get(
                    "/works/$encodedWorkId/assets",
                    type,
                )
                envelope.requireData().sortedBy { it.orderIndex }.map {
                    WorkAsset(
                        assetId = it.assetId,
                        workId = it.workId,
                        projectId = it.projectId,
                        outputKind = it.outputKind,
                        assetType = it.assetType,
                        orderIndex = it.orderIndex,
                        title = it.title,
                        summary = it.summary,
                        url = YrsApiConfig.assetUrl(it.url),
                        textContent = it.textContent,
                    )
                }
            }.getOrElse {
                fallback.getWorkAssets(workId)
            }
        }
    }

    override fun deleteWork(workId: String): Boolean {
        return runBlocking(Dispatchers.IO) {
            runCatching {
                val type = object : TypeToken<NetworkApiResponse<DeleteWorkPayload>>() {}.type
                val encodedWorkId = URLEncoder.encode(workId, StandardCharsets.UTF_8.name())
                val envelope: NetworkApiResponse<DeleteWorkPayload> = apiClient.delete("/works/$encodedWorkId", type)
                envelope.requireData().deleted
            }.getOrElse {
                fallback.deleteWork(workId)
            }
        }
    }

    companion object {
        fun fallbackAware(): WorkRepository {
            val fake = FakeWorkRepository()
            return NetworkWorkRepository(
                apiClient = SimpleApiClient(),
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
    val outputKind: String = "",
)

private data class StoryboardPayload(
    val sectionId: String,
    val projectId: String,
    val orderIndex: Int,
    val title: String,
    val summary: String,
    val subtitleLine: String,
    val durationLabel: String,
)

private data class DeleteWorkPayload(
    val deleted: Boolean,
)

private data class WorkAssetPayload(
    val assetId: String,
    val workId: String,
    val projectId: String,
    val outputKind: String,
    val assetType: String,
    val orderIndex: Int,
    val title: String,
    val summary: String,
    val url: String,
    val textContent: String,
)

private fun sampleComicStoryboard(projectId: String): List<StoryboardSection> {
    return List(8) { index ->
        StoryboardSection(
            sectionId = "${projectId}_panel_${index + 1}",
            title = "第 ${index + 1} 格：漫画分镜",
            summary = "这里展示小说拆解后的画面、人物动作和构图提示。",
            subtitleLine = "对白/旁白框：这一格承接上一格的冲突。",
            durationLabel = if (index < 4) "第 1 页" else "第 2 页",
        )
    }
}
