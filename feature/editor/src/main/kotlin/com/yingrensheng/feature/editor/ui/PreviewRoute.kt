package com.yingrensheng.feature.editor.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yingrensheng.core.model.creation.CreationOutputKind
import com.yingrensheng.core.model.creation.CreationSession
import com.yingrensheng.core.model.creation.PreviewAsset
import com.yingrensheng.core.model.creation.StoryboardSection
import com.yingrensheng.core.model.creation.outputKind
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.designsystem.theme.WeUiBackgroundMuted
import com.yingrensheng.core.designsystem.theme.WeUiTextSecondary
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreviewRoute(
    onContinue: () -> Unit,
) {
    val session by CreationRepositoryProvider.current.observeSession().collectAsState()
    val outputKind = session.outputKind()
    val preview = session.previewAsset ?: session.localResultPreview()

    YrsScaffold(
        title = outputKind.previewTitle(),
        subtitle = outputKind.previewSubtitle(),
    ) {
        if (preview != null) {
            when (outputKind) {
                CreationOutputKind.SHORT_VIDEO -> ShortVideoPreview(preview = preview)
                CreationOutputKind.COMIC_STORYBOARD -> ComicResultPreview(session = session, preview = preview)
                CreationOutputKind.STORY_TEXT -> StoryTextResultPreview(session = session, preview = preview)
                CreationOutputKind.CHARACTER_STORY -> CharacterResultPreview(session = session, preview = preview)
            }
            YrsPrimaryButton(
                text = outputKind.exportButtonText(),
                onClick = onContinue,
            )
        }
    }
}

@Composable
private fun ShortVideoPreview(preview: PreviewAsset) {
    YrsSurfaceCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(WeUiBackgroundMuted),
                contentAlignment = Alignment.Center,
            ) {
                if (preview.videoUrl.isNotBlank()) {
                    RemoteVideoPlayer(videoUrl = preview.videoUrl, title = preview.title)
                } else {
                    RemotePreviewImage(
                        imageUrl = preview.coverUrl,
                        title = preview.title,
                        emptyText = "短视频首帧生成中",
                    )
                }
            }
            Text(text = preview.title, style = MaterialTheme.typography.titleLarge)
            Text(text = preview.subtitleSummary)
            PreviewPills(preview = preview, outputKind = CreationOutputKind.SHORT_VIDEO)
            AssetUrlLine(label = "封面资源", value = preview.coverUrl)
            AssetUrlLine(label = "视频资源", value = preview.videoUrl.ifBlank { "DashScope 视频仍在生成或暂未返回地址" })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComicResultPreview(
    session: CreationSession,
    preview: PreviewAsset,
) {
    YrsSurfaceCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = preview.title, style = MaterialTheme.typography.titleLarge)
            Text(text = preview.subtitleSummary)
            PreviewPills(preview = preview, outputKind = CreationOutputKind.COMIC_STORYBOARD)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                session.storyboard.forEachIndexed { index, section ->
                    ComicPreviewPanel(index = index, section = section)
                }
            }
        }
    }
}

@Composable
private fun StoryTextResultPreview(
    session: CreationSession,
    preview: PreviewAsset,
) {
    YrsSurfaceCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = preview.title, style = MaterialTheme.typography.titleLarge)
            Text(text = preview.subtitleSummary)
            PreviewPills(preview = preview, outputKind = CreationOutputKind.STORY_TEXT)
            session.storyDraft?.let { draft ->
                Text(text = draft.opening, style = MaterialTheme.typography.titleMedium)
                Text(text = draft.body)
                Text(text = draft.closing, color = WeUiTextSecondary)
            }
            session.storyboard.forEachIndexed { index, section ->
                ResultSectionLine(prefix = "第 ${index + 1} 章", section = section)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CharacterResultPreview(
    session: CreationSession,
    preview: PreviewAsset,
) {
    YrsSurfaceCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = preview.title, style = MaterialTheme.typography.titleLarge)
            Text(text = preview.subtitleSummary)
            PreviewPills(preview = preview, outputKind = CreationOutputKind.CHARACTER_STORY)
            if (session.materials.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    session.materials.forEach { material ->
                        InfoPill(text = material.title)
                    }
                }
            }
            session.storyboard.forEach { section ->
                ResultSectionLine(prefix = section.durationLabel, section = section)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreviewPills(
    preview: PreviewAsset,
    outputKind: CreationOutputKind,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoPill(text = preview.musicLabel)
        InfoPill(text = preview.coverCaption)
        InfoPill(text = outputKind.resultPill())
    }
}

@Composable
private fun ComicPreviewPanel(
    index: Int,
    section: StoryboardSection,
) {
    Column(
        modifier = Modifier
            .widthIn(min = 138.dp)
            .fillMaxWidth()
            .background(WeUiBackgroundMuted, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(if (index % 4 == 0 || index % 4 == 3) 2.1f else 1.1f)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "格 ${index + 1}", color = WeUiTextSecondary)
        }
        Text(text = section.title, style = MaterialTheme.typography.titleSmall)
        Text(text = section.subtitleLine, color = WeUiTextSecondary)
    }
}

@Composable
private fun ResultSectionLine(
    prefix: String,
    section: StoryboardSection,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WeUiBackgroundMuted, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        InfoPill(text = prefix)
        Text(text = section.title, style = MaterialTheme.typography.titleMedium)
        Text(text = section.summary)
        Text(text = section.subtitleLine, color = WeUiTextSecondary)
    }
}

@Composable
private fun AssetUrlLine(
    label: String,
    value: String,
) {
    SelectionContainer {
        Text(
            text = "$label：$value",
            color = WeUiTextSecondary,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private fun com.yingrensheng.core.model.creation.CreationSession.localResultPreview(): PreviewAsset? {
    val draft = storyDraft ?: return null
    val kind = outputKind()
    return PreviewAsset(
        title = draft.title,
        subtitleSummary = when (kind) {
            CreationOutputKind.STORY_TEXT -> "已生成小说创作稿和结构化剧情，可导出文本创作包。"
            CreationOutputKind.COMIC_STORYBOARD -> "已生成连环漫画分镜脚本，可导出漫画分镜包。"
            CreationOutputKind.CHARACTER_STORY -> "已生成角色故事设定和剧情分镜，可导出角色故事包。"
            CreationOutputKind.SHORT_VIDEO -> "短视频预览尚未生成，请返回分镜页重新生成预览。"
        },
        musicLabel = when (kind) {
            CreationOutputKind.STORY_TEXT -> "输出：小说文本"
            CreationOutputKind.COMIC_STORYBOARD -> "输出：漫画分镜"
            CreationOutputKind.CHARACTER_STORY -> "输出：角色故事"
            CreationOutputKind.SHORT_VIDEO -> "输出：短视频预览"
        },
        coverCaption = when (kind) {
            CreationOutputKind.STORY_TEXT -> "包含：标题、开场、正文主线、结尾"
            CreationOutputKind.COMIC_STORYBOARD -> "包含：画面、字幕、节奏、分格建议"
            CreationOutputKind.CHARACTER_STORY -> "包含：角色身份、故事主线、分镜方向"
            CreationOutputKind.SHORT_VIDEO -> "包含：封面首帧、视频地址、字幕建议"
        },
    )
}

private fun CreationOutputKind.previewTitle(): String {
    return when (this) {
        CreationOutputKind.STORY_TEXT -> "小说成果"
        CreationOutputKind.COMIC_STORYBOARD -> "漫画成果"
        CreationOutputKind.SHORT_VIDEO -> "短视频预览"
        CreationOutputKind.CHARACTER_STORY -> "角色故事成果"
    }
}

private fun CreationOutputKind.previewSubtitle(): String {
    return when (this) {
        CreationOutputKind.STORY_TEXT -> "这里展示可导出的小说创作稿，不触发视频生成。"
        CreationOutputKind.COMIC_STORYBOARD -> "这里展示可导出的连环漫画分镜脚本，不触发视频生成。"
        CreationOutputKind.SHORT_VIDEO -> "这里展示后端生成的封面首帧和短视频预览。"
        CreationOutputKind.CHARACTER_STORY -> "这里展示角色设定、故事草稿和可继续扩展的创作包。"
    }
}

private fun CreationOutputKind.resultPill(): String {
    return when (this) {
        CreationOutputKind.STORY_TEXT -> "支持：导出小说创作包"
        CreationOutputKind.COMIC_STORYBOARD -> "支持：导出漫画分镜包"
        CreationOutputKind.SHORT_VIDEO -> "支持：导出短视频预览"
        CreationOutputKind.CHARACTER_STORY -> "支持：导出角色故事包"
    }
}

private fun CreationOutputKind.exportButtonText(): String {
    return when (this) {
        CreationOutputKind.STORY_TEXT -> "选择小说包导出方案"
        CreationOutputKind.COMIC_STORYBOARD -> "选择漫画包导出方案"
        CreationOutputKind.SHORT_VIDEO -> "选择视频导出方案"
        CreationOutputKind.CHARACTER_STORY -> "选择角色包导出方案"
    }
}

@Composable
private fun RemotePreviewImage(
    imageUrl: String,
    title: String,
    emptyText: String,
) {
    var bitmap by remember(imageUrl) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(imageUrl) {
        bitmap = withContext(Dispatchers.IO) {
            if (imageUrl.isBlank()) {
                null
            } else {
                runCatching {
                    URL(imageUrl).openStream().use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                }.getOrNull()
            }
        }
    }

    val image = bitmap
    if (image != null) {
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    } else {
        Text(text = emptyText)
    }
}

@Composable
private fun RemoteVideoPlayer(
    videoUrl: String,
    title: String,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var videoView by remember(videoUrl) { mutableStateOf<VideoView?>(null) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { viewContext ->
            VideoView(viewContext).apply {
                contentDescription = title
                val controller = MediaController(viewContext)
                controller.setAnchorView(this)
                setMediaController(controller)
                setVideoURI(Uri.parse(videoUrl))
                setOnPreparedListener { player ->
                    player.isLooping = false
                    seekTo(1)
                }
                videoView = this
            }
        },
        update = { view ->
            if (view != videoView) {
                videoView = view
            }
            if (view.tag != videoUrl) {
                view.tag = videoUrl
                view.setVideoURI(Uri.parse(videoUrl))
                view.seekTo(1)
            }
        },
    )

    DisposableEffect(context, videoUrl) {
        onDispose {
            videoView?.stopPlayback()
        }
    }
}
