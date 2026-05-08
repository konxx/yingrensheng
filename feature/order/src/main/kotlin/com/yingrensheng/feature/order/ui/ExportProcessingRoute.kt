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
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@Composable
fun ExportProcessingRoute(
    onBackToWorks: () -> Unit,
) {
    val session by CreationRepositoryProvider.current.observeSession().collectAsState()
    val task = session.renderTask

    YrsScaffold(
        title = "导出进行中",
        subtitle = "离开页面后真实版本会继续后台轮询与通知提醒，这里先展示状态恢复形态。",
    ) {
        task?.let {
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = it.stage, style = MaterialTheme.typography.titleLarge)
                    LinearProgressIndicator(progress = { it.progress / 100f })
                    Text(text = "进度 ${it.progress}% · 预计剩余 ${it.estimatedRemainingSeconds} 秒")
                    Text(text = "完成后会进入 ${AppRoute.Works} 对应的作品列表。")
                }
            }
        }
        YrsPrimaryButton(
            text = "返回作品库",
            onClick = onBackToWorks,
        )
    }
}
