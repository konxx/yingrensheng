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
        title = "正在连接映人生服务",
        subtitle = "请保持网络可用，连接成功后即可继续登录和创作。",
    ) {
        YrsSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = when (backendReady) {
                        null -> "正在连接服务"
                        true -> "服务连接正常"
                        false -> "暂时无法连接服务"
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = when (backendReady) {
                        null -> "正在为你准备创作环境，请稍候。"
                        true -> "连接已恢复，可以继续进入账号页面。"
                        false -> "当前网络或服务暂时不可用，请稍后重试。"
                    },
                )
            }
        }
        if (backendReady == true) {
            YrsPrimaryButton(text = "继续", onClick = onContinue)
        } else if (backendReady == false) {
            YrsPrimaryButton(text = "重新连接", onClick = onRetry)
        }
    }
}
