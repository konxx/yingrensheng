package com.yingrensheng.feature.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.scaffold.YrsScaffold

@Composable
fun OnboardingRoute(
    onContinue: () -> Unit,
) {
    YrsScaffold(
        title = "把照片，做成一支值得反复看的片子",
        subtitle = "映人生先帮你整理素材、理解情绪，再生成第一版成片。",
    ) {
        YrsSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "三步开始",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(text = "1. 导入照片、视频、录音或一句话主题")
                Text(text = "2. 选择你想表达的场景与情绪")
                Text(text = "3. 先拿到结果，再慢慢微调")
            }
        }
        YrsPrimaryButton(
            text = "开始了解",
            modifier = Modifier.fillMaxWidth(),
            onClick = onContinue,
        )
    }
}

