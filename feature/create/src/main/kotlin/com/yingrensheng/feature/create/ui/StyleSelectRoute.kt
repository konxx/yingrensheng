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
        title = "选择叙事风格",
        subtitle = "风格会同时影响小说文风、分镜密度、旁白口吻、画面提示和封面方向。",
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
