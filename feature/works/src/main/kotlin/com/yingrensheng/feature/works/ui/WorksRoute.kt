package com.yingrensheng.feature.works.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.common.util.Formatters
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.work.repository.WorkRepositoryProvider

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorksRoute() {
    val works = WorkRepositoryProvider.current.getWorks()

    YrsScaffold(
        title = "我的人生影像库",
        subtitle = "这里不是冷冰冰的文件列表，而是作品、草稿和生成状态的统一回流页。",
    ) {
        works.forEach { work ->
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = work.title, style = MaterialTheme.typography.titleLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoPill(text = work.sceneLabel)
                        InfoPill(text = work.durationLabel)
                        InfoPill(text = work.statusLabel)
                    }
                    Text(text = "最近更新 ${Formatters.formatShortTime(work.updatedAt)}")
                }
            }
        }
    }
}
