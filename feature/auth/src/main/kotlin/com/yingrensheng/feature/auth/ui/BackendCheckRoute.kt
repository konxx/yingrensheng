package com.yingrensheng.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.scaffold.YrsScaffold

@Composable
fun BackendCheckRoute(
    backendReady: Boolean?,
    onRetry: () -> Unit,
    onContinue: () -> Unit,
) {
    YrsScaffold(
        title = "启动先确认后端可用",
        subtitle = "这版 app 会优先连本地后端，只有后端在线后才继续进入登录注册。",
    ) {
        YrsSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = when (backendReady) {
                        null -> "正在检测后端连接"
                        true -> "后端连接正常"
                        false -> "后端暂时不可达"
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = when (backendReady) {
                        null -> "正在尝试连接模拟器访问宿主机地址 `10.0.2.2:3000`。"
                        true -> "已确认 `10.0.2.2:3000` 可用，可以继续进入账号体系。"
                        false -> "请先启动本地后端 `python run_server.py`，然后再重试。"
                    },
                )
            }
        }
        if (backendReady == true) {
            YrsPrimaryButton(text = "继续", onClick = onContinue)
        } else if (backendReady == false) {
            YrsPrimaryButton(text = "重新检测", onClick = onRetry)
        }
    }
}
