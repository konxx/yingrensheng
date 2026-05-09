package com.yingrensheng.feature.works.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.common.util.Formatters
import com.yingrensheng.core.designsystem.theme.WeUiBorder
import com.yingrensheng.core.designsystem.theme.WeUiSurface
import com.yingrensheng.core.designsystem.theme.WeUiTextSecondary
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.work.repository.WorkRepositoryProvider

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorksRoute() {
    val works = WorkRepositoryProvider.current.getWorks()

    YrsScaffold(
        title = "我的创作",
        subtitle = "作品、草稿与处理中的项目都会在这里回流，方便继续编辑和分享。",
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WeUiSurface, RoundedCornerShape(18.dp))
                .border(1.dp, WeUiBorder, RoundedCornerShape(18.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            WorksSummaryCell(label = "全部作品", value = works.size.toString())
            WorksSummaryCell(label = "已完成", value = works.count { it.statusLabel.contains("完成") }.toString())
            WorksSummaryCell(label = "处理中", value = works.count { it.statusLabel.contains("中") }.toString())
        }

        works.forEach { work ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WeUiSurface, RoundedCornerShape(18.dp))
                    .border(1.dp, WeUiBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(text = work.title, style = MaterialTheme.typography.titleLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoPill(text = work.sceneLabel)
                    InfoPill(text = work.durationLabel)
                    InfoPill(text = work.statusLabel)
                }
                Text(
                    text = "最近更新 ${Formatters.formatShortTime(work.updatedAt)}",
                    color = WeUiTextSecondary,
                )
                Text(
                    text = "推荐操作：继续编辑 / 分享 / 重新生成某一段",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WeUiTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun WorksSummaryCell(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = value, style = MaterialTheme.typography.titleLarge)
        Text(text = label, color = WeUiTextSecondary)
    }
}
