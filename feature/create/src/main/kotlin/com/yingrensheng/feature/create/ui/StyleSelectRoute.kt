package com.yingrensheng.feature.create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@Composable
fun StyleSelectRoute(
    onContinue: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    YrsScaffold(
        title = "不要只选模板，先选情绪",
        subtitle = "情绪会同时影响旁白口吻、音乐节奏、封面和字幕样式。",
    ) {
        creationRepository.styles().forEach { style ->
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = style.title, style = MaterialTheme.typography.titleLarge)
                    Text(text = style.description)
                    YrsPrimaryButton(
                        text = "使用 ${style.title}",
                        onClick = {
                            creationRepository.selectStyle(style)
                            onContinue()
                        },
                    )
                }
            }
        }
    }
}
