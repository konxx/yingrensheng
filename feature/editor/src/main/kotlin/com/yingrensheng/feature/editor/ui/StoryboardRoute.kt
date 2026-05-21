package com.yingrensheng.feature.editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.model.creation.CreationOutputKind
import com.yingrensheng.core.model.creation.StoryboardSection
import com.yingrensheng.core.model.creation.outputKind
import com.yingrensheng.core.model.creation.requiresVideoPreview
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StoryboardRoute(
    onContinue: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    val session by creationRepository.observeSession().collectAsState()
    val scope = rememberCoroutineScope()
    var generatingPreview by remember { mutableStateOf(false) }
    val outputKind = session.outputKind()

    YrsScaffold(
        title = outputKind.storyboardTitle(),
        subtitle = outputKind.storyboardSubtitle(),
    ) {
        when (outputKind) {
            CreationOutputKind.COMIC_STORYBOARD -> {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    session.storyboard.forEachIndexed { index, section ->
                        ComicPanelCard(index = index, section = section)
                    }
                }
            }
            CreationOutputKind.SHORT_VIDEO -> {
                session.storyboard.forEachIndexed { index, section ->
                    VideoShotCard(index = index, section = section)
                }
            }
            CreationOutputKind.STORY_TEXT -> {
                session.storyboard.forEachIndexed { index, section ->
                    NovelChapterCard(index = index, section = section)
                }
            }
            CreationOutputKind.CHARACTER_STORY -> {
                session.storyboard.forEach { section ->
                    CharacterStoryCard(section = section)
                }
            }
        }
        YrsPrimaryButton(
            text = if (generatingPreview) "正在生成预览" else outputKind.previewButtonText(),
            onClick = {
                if (!generatingPreview) {
                    scope.launch {
                        generatingPreview = true
                        try {
                            if (session.requiresVideoPreview()) {
                                creationRepository.createPreview()
                            }
                            onContinue()
                        } finally {
                            generatingPreview = false
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun ComicPanelCard(
    index: Int,
    section: StoryboardSection,
) {
    Column(
        modifier = Modifier
            .widthIn(min = 148.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(if (index % 4 == 0 || index % 4 == 3) 2.1f else 1.15f)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "第 ${index + 1} 格", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text = section.title, style = MaterialTheme.typography.titleMedium)
        Text(text = section.summary)
        Text(text = section.subtitleLine, color = MaterialTheme.colorScheme.onSurfaceVariant)
        InfoPill(text = section.durationLabel)
    }
}

@Composable
private fun VideoShotCard(
    index: Int,
    section: StoryboardSection,
) {
    YrsSurfaceCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoPill(text = "镜头 ${index + 1}")
            Text(text = section.title, style = MaterialTheme.typography.titleLarge)
            Text(text = section.summary)
            Text(text = section.subtitleLine, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "时长 ${section.durationLabel}")
        }
    }
}

@Composable
private fun NovelChapterCard(
    index: Int,
    section: StoryboardSection,
) {
    YrsSurfaceCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoPill(text = "章节 ${index + 1}")
            Text(text = section.title, style = MaterialTheme.typography.titleLarge)
            Text(text = section.summary)
            Text(text = section.subtitleLine, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = section.durationLabel)
        }
    }
}

@Composable
private fun CharacterStoryCard(section: StoryboardSection) {
    YrsSurfaceCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = section.title, style = MaterialTheme.typography.titleLarge)
            Text(text = section.summary)
            Text(text = section.subtitleLine, color = MaterialTheme.colorScheme.onSurfaceVariant)
            InfoPill(text = section.durationLabel)
        }
    }
}

private fun CreationOutputKind.storyboardTitle(): String {
    return when (this) {
        CreationOutputKind.STORY_TEXT -> "小说章节正文"
        CreationOutputKind.COMIC_STORYBOARD -> "连环漫画分镜"
        CreationOutputKind.SHORT_VIDEO -> "短视频分镜"
        CreationOutputKind.CHARACTER_STORY -> "角色故事分镜"
    }
}

private fun CreationOutputKind.storyboardSubtitle(): String {
    return when (this) {
        CreationOutputKind.STORY_TEXT -> "这里展示可阅读的分章正文，不会进入视频生成流程。"
        CreationOutputKind.COMIC_STORYBOARD -> "这里输出漫画格画面、字幕和剧情节奏，不会自动跳到视频生成。"
        CreationOutputKind.SHORT_VIDEO -> "这里承接镜头、旁白、字幕和画面提示，下一步生成短视频预览。"
        CreationOutputKind.CHARACTER_STORY -> "这里整理角色身份、剧情钩子和后续可扩展的漫画/短视频方向。"
    }
}

private fun CreationOutputKind.previewButtonText(): String {
    return when (this) {
        CreationOutputKind.STORY_TEXT -> "查看小说成果"
        CreationOutputKind.COMIC_STORYBOARD -> "查看漫画成果"
        CreationOutputKind.SHORT_VIDEO -> "生成短视频预览"
        CreationOutputKind.CHARACTER_STORY -> "查看角色故事成果"
    }
}
