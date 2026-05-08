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
fun AgreementRoute(
    onAccept: () -> Unit,
) {
    YrsScaffold(
        title = "在创作前，把边界说清楚",
        subtitle = "涉及家人、未成年人和 AI 生成内容时，授权与标识需要自然进入流程。",
    ) {
        YrsSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "你将确认",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(text = "已阅读用户协议与隐私政策")
                Text(text = "确认素材拥有使用权，并理解第三方人物提示")
                Text(text = "理解导出时会保留 AI 生成标识与授权留痕")
            }
        }
        YrsPrimaryButton(
            text = "同意并继续",
            onClick = onAccept,
        )
    }
}

