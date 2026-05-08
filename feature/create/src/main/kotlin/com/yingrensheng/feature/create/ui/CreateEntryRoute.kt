package com.yingrensheng.feature.create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.model.project.CreationMode
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@Composable
fun CreateEntryRoute(
    onModeSelected: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    YrsScaffold(
        title = "先决定你想怎么开始",
        subtitle = "快速成片强调 3 分钟见结果，故事成片更像 AI 导演陪你整理回忆。",
    ) {
        creationRepository.creationModes().forEach { (mode, label) ->
            val subtitle = when (mode) {
                CreationMode.QUICK_FILM -> "更适合旅行、节庆、祝福，少填信息先出片"
                CreationMode.STORY_FILM -> "更适合人生回忆、重要关系表达与更完整叙事"
            }
            YrsSurfaceCard(
                modifier = androidx.compose.ui.Modifier,
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = label, style = MaterialTheme.typography.titleLarge)
                        Text(text = subtitle)
                        com.yingrensheng.core.designsystem.component.YrsPrimaryButton(
                            text = "选择 $label",
                            onClick = {
                                creationRepository.selectMode(mode)
                                onModeSelected()
                            },
                        )
                    }
                },
            )
        }
    }
}
