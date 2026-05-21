package com.yingrensheng.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.common.util.Formatters
import com.yingrensheng.core.designsystem.component.YrsMetricCard
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
        subtitle = "把自拍、大纲或小说变成角色故事、连环漫画和短视频。",
    ) {
        YrsSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "今天想进入哪本故事？",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "上传照片、大纲或小说正文，按不同创作目标进入专属流程。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoPill(text = "角色架空")
                    InfoPill(text = "小说生成")
                    InfoPill(text = "漫画分镜")
                    InfoPill(text = "短视频")
                }
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
                    Text(
                        text = "${project.sceneType} · ${project.currentStep}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoPill(text = "${project.materialCount} 份素材")
                        InfoPill(text = project.moodLabel)
                        InfoPill(text = Formatters.formatShortTime(project.updatedAt))
                    }
                }
            }
        }

        SectionHeader(title = "创作资产")
        YrsSurfaceCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                YrsMetricCard(label = "项目", value = projects.size.toString())
                YrsMetricCard(label = "最近进度", value = "${latestProject?.progress ?: 0}%")
                YrsMetricCard(label = "素材", value = (latestProject?.materialCount ?: 0).toString())
            }
        }

        SectionHeader(title = "热门创作", actionLabel = "AI 导演")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("西游记穿越", "红楼梦角色", "大纲写小说", "小说转漫画", "小说转短视频").forEach { label ->
                InfoPill(text = label)
            }
        }
    }
}
