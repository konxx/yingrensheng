package com.yingrensheng.feature.order.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.navigation.route.AppRoute
import com.yingrensheng.core.model.creation.CreationOutputKind
import com.yingrensheng.core.model.creation.outputKind
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@Composable
fun ExportProcessingRoute(
    onBackToWorks: () -> Unit,
) {
    val session by CreationRepositoryProvider.current.observeSession().collectAsState()
    val task = session.renderTask
    val outputKind = session.outputKind()

    YrsScaffold(
        title = outputKind.processingTitle(),
        subtitle = outputKind.processingSubtitle(),
    ) {
        task?.let {
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = it.stage, style = MaterialTheme.typography.titleLarge)
                    LinearProgressIndicator(progress = { it.progress / 100f })
                    Text(text = "进度 ${it.progress}% · 预计剩余 ${it.estimatedRemainingSeconds} 秒")
                    Text(text = outputKind.processingDestinationText())
                }
            }
        }
        YrsPrimaryButton(
            text = "返回作品库",
            onClick = onBackToWorks,
        )
    }
}

private fun CreationOutputKind.processingDestinationText(): String {
    return when (this) {
        CreationOutputKind.STORY_TEXT -> "完成后会进入作品列表，详情页展示小说包记录。"
        CreationOutputKind.COMIC_STORYBOARD -> "完成后会进入作品列表，详情页展示漫画分镜包记录，不展示视频播放器。"
        CreationOutputKind.CHARACTER_STORY -> "完成后会进入作品列表，详情页展示角色故事包记录。"
        CreationOutputKind.SHORT_VIDEO -> "完成后会进入 ${AppRoute.Works} 对应的作品列表，可查看封面和视频资源。"
    }
}

private fun CreationOutputKind.processingTitle(): String {
    return when (this) {
        CreationOutputKind.STORY_TEXT -> "小说包已生成"
        CreationOutputKind.COMIC_STORYBOARD -> "漫画分镜包已生成"
        CreationOutputKind.CHARACTER_STORY -> "角色故事包已生成"
        CreationOutputKind.SHORT_VIDEO -> "导出进行中"
    }
}

private fun CreationOutputKind.processingSubtitle(): String {
    return when (this) {
        CreationOutputKind.STORY_TEXT -> "当前结果为小说文本创作包，不进入视频生成流程。"
        CreationOutputKind.COMIC_STORYBOARD -> "当前结果为连环漫画分镜包，不进入视频生成流程。"
        CreationOutputKind.CHARACTER_STORY -> "当前结果为角色故事创作包，不进入视频生成流程。"
        CreationOutputKind.SHORT_VIDEO -> "离开页面后真实版本会继续后台轮询与通知提醒，这里先展示状态恢复形态。"
    }
}
