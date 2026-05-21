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
import com.yingrensheng.core.model.creation.CreationOutputKind
import com.yingrensheng.core.model.creation.outputKind
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
    val task = session.renderTask
    val outputKind = session.outputKind()

    YrsScaffold(
        title = outputKind.draftTitle(),
        subtitle = outputKind.draftSubtitle(),
    ) {
        if (draft == null) {
            if (generating || task != null) {
                YrsSurfaceCard {
                    Text(
                        text = if (generating) "正在生成创作草稿，请稍候" else task?.stage.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                    )
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
                text = if (generating) "重新生成中" else "重新调用 AI 生成草稿",
                enabled = !generating,
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
            YrsPrimaryButton(
                text = outputKind.draftContinueText(),
                onClick = onContinue,
            )
        }
    }
}

private fun CreationOutputKind.draftTitle(): String {
    return when (this) {
        CreationOutputKind.CHARACTER_STORY -> "确认角色故事草稿"
        CreationOutputKind.STORY_TEXT -> "确认小说第一稿"
        CreationOutputKind.COMIC_STORYBOARD -> "确认漫画改编草稿"
        CreationOutputKind.SHORT_VIDEO -> "确认短视频脚本草稿"
    }
}

private fun CreationOutputKind.draftSubtitle(): String {
    return when (this) {
        CreationOutputKind.CHARACTER_STORY -> "先确认角色身份、关系和第一幕剧情，再整理角色故事包。"
        CreationOutputKind.STORY_TEXT -> "先确认小说主线和正文方向，再生成可阅读的章节正文。"
        CreationOutputKind.COMIC_STORYBOARD -> "先确认漫画改编方向，再拆成格子画面和对白。"
        CreationOutputKind.SHORT_VIDEO -> "先确认故事钩子和脚本方向，再拆镜头并生成视频预览。"
    }
}

private fun CreationOutputKind.draftContinueText(): String {
    return when (this) {
        CreationOutputKind.CHARACTER_STORY -> "整理角色故事包"
        CreationOutputKind.STORY_TEXT -> "生成章节正文"
        CreationOutputKind.COMIC_STORYBOARD -> "拆成漫画分格"
        CreationOutputKind.SHORT_VIDEO -> "生成视频分镜"
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
