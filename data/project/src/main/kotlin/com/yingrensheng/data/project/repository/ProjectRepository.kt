package com.yingrensheng.data.project.repository

import com.yingrensheng.core.common.util.IdGenerator
import com.yingrensheng.core.model.project.CreationMode
import com.yingrensheng.core.model.project.Project
import com.yingrensheng.core.model.project.ProjectStatus
import java.time.Instant
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ProjectRepository {
    fun observeProjects(): StateFlow<List<Project>>

    fun latestProject(): Project?

    fun createDraft(title: String, sceneType: String, mode: CreationMode): Project
}

object ProjectRepositoryProvider {
    @Volatile
    var current: ProjectRepository = NetworkProjectRepository.fallbackAware()
}

class FakeProjectRepository : ProjectRepository {
    private val now = Instant.now()

    private val projectsState = kotlinx.coroutines.flow.MutableStateFlow(
        listOf(
            Project(
                projectId = "project_travel",
                title = "大理五月风",
                sceneType = "旅行纪念",
                mode = CreationMode.QUICK_FILM,
                status = ProjectStatus.PREVIEW_READY,
                progress = 100,
                currentStep = "首版已生成",
                materialCount = 24,
                moodLabel = "温暖",
                updatedAt = now,
            ),
            Project(
                projectId = "project_memory",
                title = "给妈妈的回忆片",
                sceneType = "人生回忆",
                mode = CreationMode.STORY_FILM,
                status = ProjectStatus.GENERATING,
                progress = 68,
                currentStep = "AI 正在编排故事板",
                materialCount = 41,
                moodLabel = "怀念",
                updatedAt = now.minusSeconds(3600),
            ),
        ),
    )

    override fun observeProjects(): StateFlow<List<Project>> = projectsState.asStateFlow()

    override fun latestProject(): Project? = projectsState.value.firstOrNull()

    override fun createDraft(title: String, sceneType: String, mode: CreationMode): Project {
        val newProject = Project(
            projectId = IdGenerator.newId("project"),
            title = title,
            sceneType = sceneType,
            mode = mode,
            status = ProjectStatus.DRAFT,
            progress = 8,
            currentStep = "已创建草稿",
            materialCount = 0,
            moodLabel = "待选择",
            updatedAt = Instant.now(),
        )
        projectsState.value = listOf(newProject) + projectsState.value
        return newProject
    }
}
