package com.yingrensheng.feature.works.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.common.util.Formatters
import com.yingrensheng.core.designsystem.theme.WeUiBorder
import com.yingrensheng.core.designsystem.theme.WeUiSurface
import com.yingrensheng.core.designsystem.theme.WeUiTextSecondary
import com.yingrensheng.core.model.work.Work
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.work.repository.WorkRepositoryProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun WorksRoute(
    onOpenWork: (Work) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = WorkRepositoryProvider.current
    var refreshVersion by remember { mutableIntStateOf(0) }
    var pendingDeleteWork by remember { mutableStateOf<Work?>(null) }
    val works = remember(refreshVersion) { repository.getWorks() }

    YrsScaffold(
        title = "作品",
        subtitle = "这里展示真实作品记录，点进每一张卡都可以继续查看当前状态和资源地址。",
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
                    .combinedClickable(
                        onClick = { onOpenWork(work) },
                        onLongClick = { pendingDeleteWork = work },
                    )
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
                Text(
                    text = "查看详情 >",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }

    pendingDeleteWork?.let { work ->
        AlertDialog(
            onDismissRequest = { pendingDeleteWork = null },
            title = { Text(text = "删除作品") },
            text = { Text(text = "确定删除《${work.title}》吗？删除后作品列表中不会再显示这条记录。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteWork = null
                        scope.launch {
                            val deleted = withContext(Dispatchers.IO) {
                                repository.deleteWork(work.workId)
                            }
                            refreshVersion += 1
                            android.widget.Toast.makeText(
                                context,
                                if (deleted) "作品已删除" else "作品记录不存在",
                                android.widget.Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                ) {
                    Text(text = "删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteWork = null }) {
                    Text(text = "取消")
                }
            },
        )
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
