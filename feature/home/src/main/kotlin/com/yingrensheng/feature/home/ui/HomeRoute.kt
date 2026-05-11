package com.yingrensheng.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.common.util.Formatters
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.component.SectionHeader
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.project.repository.ProjectRepositoryProvider
import com.yingrensheng.data.user.repository.UserRepositoryProvider

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeRoute(
    onStartCreate: () -> Unit,
) {
    val userRepository = UserRepositoryProvider.current
    val projectRepository = ProjectRepositoryProvider.current
    val session by userRepository.session().collectAsState()
    val projects by projectRepository.observeProjects().collectAsState()
    val latestProject = projects.firstOrNull()

    YrsScaffold(
        title = "欢迎回来，${session.user?.nickname ?: "映人生用户"}",
        subtitle = "先给你一个能快速开拍的入口，再把最近项目稳稳接住。",
    ) {
        YrsSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "今天适合开始一支什么片子？",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(text = "旅行纪念、人生回忆、节庆祝福都可以从一键开始进入。")
                YrsPrimaryButton(
                    text = "开始创作",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onStartCreate,
                )
            }
        }

        latestProject?.let { project ->
            SectionHeader(title = "继续上次创作", actionLabel = "${project.progress}%")
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = project.title, style = MaterialTheme.typography.titleLarge)
                    Text(text = "${project.sceneType} · ${project.currentStep}")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoPill(text = "${project.materialCount} 份素材")
                        InfoPill(text = project.moodLabel)
                        InfoPill(text = Formatters.formatShortTime(project.updatedAt))
                    }
                }
            }
        }

        SectionHeader(title = "推荐场景", actionLabel = "更懂情绪")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("旅行纪念", "人生回忆", "节庆祝福", "主角故事").forEach { label ->
                InfoPill(text = label)
            }
        }
    }
}
