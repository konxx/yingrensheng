package com.yingrensheng.feature.create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@Composable
fun MaterialReviewRoute(
    onContinue: () -> Unit,
) {
    val session by CreationRepositoryProvider.current.observeSession().collectAsState()

    YrsScaffold(
        title = "素材已经先帮你整理了一轮",
        subtitle = "这里强化的是“整理素材”而不是“上传文件”。",
    ) {
        session.materials.forEach { material ->
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = material.title, style = MaterialTheme.typography.titleLarge)
                    InfoPill(text = "${material.type.name} ${material.durationLabel}".trim())
                    Text(text = material.insight)
                }
            }
        }
        YrsPrimaryButton(
            text = "确认授权并继续",
            onClick = onContinue,
        )
    }
}
