package com.yingrensheng.feature.create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider
import kotlinx.coroutines.launch

@Composable
fun StoryDraftRoute(
    onContinue: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    val session by creationRepository.observeSession().collectAsState()
    val scope = rememberCoroutineScope()
    var generating by remember { mutableStateOf(false) }
    val draft = session.storyDraft

    YrsScaffold(
        title = "确认第一版创作稿",
        subtitle = "先生成角色设定、小说主线和改编方向，再进入漫画/短视频分镜。",
    ) {
        if (draft == null) {
            if (generating) {
                YrsSurfaceCard {
                    Text(text = "正在生成创作草稿，请稍候", style = MaterialTheme.typography.titleMedium)
                }
            }
            YrsPrimaryButton(
                text = if (generating) "生成中" else "生成创作草稿",
                onClick = {
                    if (!generating) {
                        scope.launch {
                            generating = true
                            try {
                                creationRepository.generateStoryDraft()
                            } finally {
                                generating = false
                            }
                        }
                    }
                },
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
