package com.yingrensheng.feature.create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SceneSelectRoute(
    onSceneSelected: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    YrsScaffold(
        title = "用场景告诉 AI 你想得到什么",
        subtitle = "每张卡都直接描述结果预期、成片时长和等待时间。",
    ) {
        creationRepository.scenes().forEach { scene ->
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = scene.title, style = MaterialTheme.typography.titleLarge)
                    Text(text = scene.subtitle)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoPill(text = scene.recommendedDurationLabel)
                        InfoPill(text = scene.estimatedTimeLabel)
                    }
                    YrsPrimaryButton(
                        text = "选择 ${scene.title}",
                        onClick = {
                            creationRepository.selectScene(scene)
                            onSceneSelected()
                        },
                    )
                }
            }
        }
    }
}
