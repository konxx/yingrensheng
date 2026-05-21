package com.yingrensheng.feature.create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LinearProgressIndicator
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
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider
import kotlinx.coroutines.launch

@Composable
fun StoryGeneratingRoute(
    onContinue: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    val session by creationRepository.observeSession().collectAsState()
    val scope = rememberCoroutineScope()
    var generatingDraft by remember { mutableStateOf(false) }
    var generatingStoryboard by remember { mutableStateOf(false) }
    val task = session.renderTask
    val outputKind = session.outputKind()

    YrsScaffold(
        title = outputKind.generatingTitle(),
        subtitle = outputKind.generatingSubtitle(session.selectedScene?.sceneId.orEmpty()),
    ) {
        if (task == null) {
            YrsPrimaryButton(
                text = if (generatingDraft) "生成中" else outputKind.startText(),
                onClick = {
                    if (!generatingDraft) {
                        scope.launch {
                            generatingDraft = true
                            try {
                                creationRepository.generateStoryDraft()
                            } finally {
                                generatingDraft = false
                            }
                        }
                    }
                },
            )
        } else {
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = task.stage, style = MaterialTheme.typography.titleLarge)
                    LinearProgressIndicator(progress = { task.progress / 100f })
                    Text(text = "进度 ${task.progress}% · 预计 ${task.estimatedRemainingSeconds} 秒")
                }
            }
            YrsPrimaryButton(
                text = if (generatingStoryboard) outputKind.buildingText() else outputKind.nextText(session.storyboard.isEmpty()),
                onClick = {
                    if (!generatingStoryboard) {
                        scope.launch {
                            generatingStoryboard = true
                            try {
                                creationRepository.buildStoryboard()
                                if (creationRepository.observeSession().value.storyboard.isNotEmpty()) {
                                    onContinue()
                                }
                            } finally {
                                generatingStoryboard = false
                            }
                        }
                    }
                },
            )
        }
    }
}

private fun CreationOutputKind.generatingTitle(): String {
    return when (this) {
        CreationOutputKind.CHARACTER_STORY -> "生成角色故事包"
        CreationOutputKind.STORY_TEXT -> "生成小说正文"
        CreationOutputKind.COMIC_STORYBOARD -> "拆解连环漫画"
        CreationOutputKind.SHORT_VIDEO -> "拆解短视频脚本"
    }
}

private fun CreationOutputKind.generatingSubtitle(sceneId: String): String {
    return when (this) {
        CreationOutputKind.CHARACTER_STORY -> if (sceneId == "scene_character_honglou") {
            "AI 会把照片气质、贾府身份和人物关系整理成角色设定与第一幕剧情。"
        } else {
            "AI 会把照片气质、西游身份和取经路冲突整理成角色设定与第一幕剧情。"
        }
        CreationOutputKind.STORY_TEXT -> "AI 会把输入扩写为可阅读的分章正文和人物关系，不进入视频生成。"
        CreationOutputKind.COMIC_STORYBOARD -> "AI 会把小说拆成格子画面、人物动作、对白和旁白框，不进入视频预览。"
        CreationOutputKind.SHORT_VIDEO -> "AI 会把小说拆成前 5 秒钩子、镜头推进、旁白字幕和首帧提示。"
    }
}

private fun CreationOutputKind.startText(): String {
    return when (this) {
        CreationOutputKind.CHARACTER_STORY -> "开始生成角色故事"
        CreationOutputKind.STORY_TEXT -> "开始生成小说正文"
        CreationOutputKind.COMIC_STORYBOARD -> "开始拆漫画脚本"
        CreationOutputKind.SHORT_VIDEO -> "开始拆视频脚本"
    }
}

private fun CreationOutputKind.buildingText(): String {
    return when (this) {
        CreationOutputKind.CHARACTER_STORY -> "正在整理角色故事"
        CreationOutputKind.STORY_TEXT -> "正在生成章节正文"
        CreationOutputKind.COMIC_STORYBOARD -> "正在生成漫画分格"
        CreationOutputKind.SHORT_VIDEO -> "正在生成视频分镜"
    }
}

private fun CreationOutputKind.nextText(shouldRetry: Boolean): String {
    if (shouldRetry) {
        return when (this) {
            CreationOutputKind.CHARACTER_STORY -> "整理角色故事包"
            CreationOutputKind.STORY_TEXT -> "生成章节正文"
            CreationOutputKind.COMIC_STORYBOARD -> "拆成漫画分格"
            CreationOutputKind.SHORT_VIDEO -> "生成视频分镜"
        }
    }
    return when (this) {
        CreationOutputKind.CHARACTER_STORY -> "查看角色设定"
        CreationOutputKind.STORY_TEXT -> "查看章节正文"
        CreationOutputKind.COMIC_STORYBOARD -> "查看漫画分格"
        CreationOutputKind.SHORT_VIDEO -> "查看视频分镜"
    }
}
