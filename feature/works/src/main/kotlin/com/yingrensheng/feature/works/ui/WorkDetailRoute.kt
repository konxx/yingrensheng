package com.yingrensheng.feature.works.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.common.util.Formatters
import com.yingrensheng.core.designsystem.theme.WeUiBorder
import com.yingrensheng.core.designsystem.theme.WeUiSurface
import com.yingrensheng.core.designsystem.theme.WeUiTextSecondary
import com.yingrensheng.core.model.work.Work
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold

@Composable
fun WorkDetailRoute(
    work: Work?,
) {
    YrsScaffold(
        title = work?.title ?: "作品详情",
        subtitle = if (work == null) {
            "当前没有可展示的作品，请从作品列表重新进入。"
        } else {
            "这里展示该作品的真实导出状态、所属项目以及当前生成出来的资源地址。"
        },
    ) {
        if (work == null) {
            Text(text = "未找到作品数据。")
            return@YrsScaffold
        }

        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WeUiSurface, RoundedCornerShape(18.dp))
                    .border(1.dp, WeUiBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(text = work.title, style = MaterialTheme.typography.titleLarge)
                InfoPill(text = work.sceneLabel)
                InfoPill(text = work.durationLabel)
                InfoPill(text = work.statusLabel)
                Text(text = "项目 ID：${work.projectId}", color = WeUiTextSecondary)
                Text(text = "作品 ID：${work.workId}", color = WeUiTextSecondary)
                Text(
                    text = "最近更新 ${Formatters.formatShortTime(work.updatedAt)}",
                    color = WeUiTextSecondary,
                )
            }

            AssetBlock(
                title = "封面资源",
                value = work.coverUrl.ifBlank { "暂未生成封面地址" },
            )
            AssetBlock(
                title = "视频资源",
                value = work.videoUrl.ifBlank { "暂未生成视频地址" },
            )
        }
    }
}

@Composable
private fun AssetBlock(
    title: String,
    value: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WeUiSurface, RoundedCornerShape(18.dp))
            .border(1.dp, WeUiBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        SelectionContainer {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = WeUiTextSecondary,
            )
        }
    }
}
