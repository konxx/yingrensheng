package com.yingrensheng.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.scaffold.YrsScaffold

@Composable
fun SettingsRoute(
    onLogout: () -> Unit = {},
) {
    var notifyEnabled by remember { mutableStateOf(true) }
    var wifiOnly by remember { mutableStateOf(true) }
    var autoResume by remember { mutableStateOf(true) }

    YrsScaffold(
        title = "设置",
        subtitle = "把常用开关、上传偏好和基础说明都集中在一页内处理。",
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            YrsSurfaceCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                SettingSwitchCell(
                    title = "任务完成提醒",
                    subtitle = "生成、导出完成后提醒我",
                    checked = notifyEnabled,
                    onCheckedChange = { notifyEnabled = it },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingSwitchCell(
                    title = "仅 Wi-Fi 上传",
                    subtitle = "避免大素材上传消耗移动流量",
                    checked = wifiOnly,
                    onCheckedChange = { wifiOnly = it },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingSwitchCell(
                    title = "自动恢复创作",
                    subtitle = "重新打开 app 后继续上次项目",
                    checked = autoResume,
                    onCheckedChange = { autoResume = it },
                )
            }
        }

        YrsSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("缓存与存储", style = MaterialTheme.typography.titleLarge)
            Text("当前缓存策略会优先保留缩略图、最近预览封面和未完成草稿。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("预计缓存占用")
                Text("128 MB", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            }
        }

        YrsSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("关于映人生", style = MaterialTheme.typography.titleLarge)
                Text("版本 0.1.0", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("映人生是一个把照片、视频和几句心里话整理成短片的影像创作工具。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        YrsSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("账号", style = MaterialTheme.typography.titleLarge)
                Text("退出后会清除本机保存的登录状态，并关闭自动登录。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onLogout) {
                    Text("退出登录")
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchCell(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
