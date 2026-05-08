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
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@Composable
fun StoryDraftRoute(
    onContinue: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    val session by creationRepository.observeSession().collectAsState()
    val draft = session.storyDraft

    YrsScaffold(
        title = "先确认故事主线",
        subtitle = "V1 先把旁白初稿与故事结构确认下来，再进入分镜生成状态。",
    ) {
        if (draft == null) {
            YrsPrimaryButton(
                text = "生成故事草稿",
                onClick = { creationRepository.generateStoryDraft() },
            )
        } else {
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = draft.title, style = MaterialTheme.typography.titleLarge)
                    Text(text = draft.opening)
                    Text(text = draft.body)
                    Text(text = draft.closing)
                }
            }
            YrsPrimaryButton(
                text = "进入分镜生成",
                onClick = onContinue,
            )
        }
    }
}
