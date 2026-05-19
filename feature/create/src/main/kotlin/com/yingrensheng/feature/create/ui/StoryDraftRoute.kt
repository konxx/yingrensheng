package com.yingrensheng.feature.create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
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
                    val directorAnswers = creationRepository.recordedDirectorAnswers()
                    if (directorAnswers.isNotEmpty()) {
                        Text(text = "已记录的 AI 导演设定", style = MaterialTheme.typography.titleMedium)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            directorAnswers.forEach { answer ->
                                InfoPill(text = answer)
                            }
                        }
                    }
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

private fun com.yingrensheng.data.creation.repository.CreationRepository.recordedDirectorAnswers(): List<String> {
    val session = observeSession().value
    return interviewPrompts().mapNotNull { prompt ->
        session.interviewAnswers[prompt.promptId]
            ?.takeIf { it.isNotBlank() }
            ?.let { "${prompt.title}：$it" }
    }
}
