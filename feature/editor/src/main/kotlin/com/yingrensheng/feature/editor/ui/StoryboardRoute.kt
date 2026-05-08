package com.yingrensheng.feature.editor.ui

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
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@Composable
fun StoryboardRoute(
    onContinue: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    val session by creationRepository.observeSession().collectAsState()

    YrsScaffold(
        title = "先用故事板，而不是复杂时间线",
        subtitle = "MVP 优先做段落卡片式故事板，把高价值改动留给用户。",
    ) {
        session.storyboard.forEach { section ->
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = section.title, style = MaterialTheme.typography.titleLarge)
                    Text(text = section.summary)
                    Text(text = section.subtitleLine)
                    Text(text = "片段时长 ${section.durationLabel}")
                }
            }
        }
        YrsPrimaryButton(
            text = "生成预览",
            onClick = {
                creationRepository.createPreview()
                onContinue()
            },
        )
    }
}
