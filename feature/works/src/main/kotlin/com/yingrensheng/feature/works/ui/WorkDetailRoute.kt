package com.yingrensheng.feature.works.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yingrensheng.core.common.util.Formatters
import com.yingrensheng.core.designsystem.theme.WeUiBorder
import com.yingrensheng.core.designsystem.theme.WeUiBackgroundMuted
import com.yingrensheng.core.designsystem.theme.WeUiSurface
import com.yingrensheng.core.designsystem.theme.WeUiTextSecondary
import com.yingrensheng.core.model.creation.StoryboardSection
import com.yingrensheng.core.model.work.Work
import com.yingrensheng.core.model.work.WorkAsset
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.work.repository.WorkRepositoryProvider
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun WorkDetailRoute(
    work: Work?,
    onDeleteWork: (Work) -> Unit = {},
) {
    var showDeleteDialog by remember(work?.workId) { mutableStateOf(false) }
    var storyboard by remember(work?.projectId) { mutableStateOf<List<StoryboardSection>>(emptyList()) }
    var workAssets by remember(work?.workId) { mutableStateOf<List<WorkAsset>>(emptyList()) }
    val isComicWork = work?.isComicWork() == true
    val isVideoWork = work?.isVideoWork() == true

    LaunchedEffect(work?.workId, work?.projectId, isComicWork) {
        val projectId = work?.projectId
        val workId = work?.workId
        workAssets = if (workId != null) {
            withContext(Dispatchers.IO) {
                WorkRepositoryProvider.current.getWorkAssets(workId)
            }
        } else {
            emptyList()
        }
        storyboard = if (projectId != null && isComicWork) {
            withContext(Dispatchers.IO) {
                WorkRepositoryProvider.current.getStoryboard(projectId)
            }
        } else {
            emptyList()
        }
    }

    YrsScaffold(
        title = work?.title ?: "作品详情",
        subtitle = if (work == null) {
            "当前没有可展示的作品，请从作品列表重新进入。"
        } else {
            if (isComicWork) {
                "这里展示该作品的真实导出状态、所属项目，以及可直接预览的连环漫画成品。"
            } else if (isVideoWork) {
                "这里展示该作品的真实导出状态、所属项目，以及可直接观看的短视频成品。"
            } else {
                "这里展示该作品的真实导出状态、所属项目，以及完整图文成品包。"
            }
        },
    ) {
        if (work == null) {
            Text(text = "未找到作品数据。")
            return@YrsScaffold
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(WeUiSurface, RoundedCornerShape(18.dp))
                .border(1.dp, WeUiBorder, RoundedCornerShape(18.dp))
                .combinedClickable(
                    onClick = {},
                    onLongClick = { showDeleteDialog = true },
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = work.title, style = MaterialTheme.typography.titleLarge)
            InfoPill(text = work.sceneLabel)
            InfoPill(text = work.durationLabel)
            InfoPill(text = work.statusLabel)
            Text(text = "项目 ID：${work.projectId}", color = WeUiTextSecondary)
            Text(text = "作品 ID：${work.workId}", color = WeUiTextSecondary)
            Text(
                text = "最近更新 ${Formatters.formatShortTime(work.updatedAt)}",
                color = WeUiTextSecondary,
            )
        }

        if (work.coverUrl.isNotBlank()) {
            AssetBlock(
                title = "封面成品",
                value = work.coverUrl,
                emptyText = "暂未生成封面地址",
                preview = {
                    RemoteCoverImage(
                        imageUrl = work.coverUrl,
                        title = work.title,
                    )
                },
            )
        }
        if (workAssets.isNotEmpty()) {
            WorkAssetsBlock(work = work, assets = workAssets)
        } else if (isComicWork) {
            ComicStoryboardBlock(storyboard = storyboard)
        } else if (isVideoWork) {
            AssetBlock(
                title = "视频成品",
                value = work.videoUrl,
                emptyText = "暂未生成视频地址",
                preview = {
                    RemoteVideoPlayer(
                        videoUrl = work.videoUrl,
                        title = work.title,
                    )
                },
            )
        }
    }

    val currentWork = work
    if (currentWork != null && showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "删除作品") },
            text = { Text(text = "确定删除《${currentWork.title}》吗？删除后作品列表中不会再显示这条记录。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteWork(currentWork)
                    },
                ) {
                    Text(text = "删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = "取消")
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkAssetsBlock(
    work: Work,
    assets: List<WorkAsset>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WeUiSurface, RoundedCornerShape(18.dp))
            .border(1.dp, WeUiBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "最终成品", style = MaterialTheme.typography.titleMedium)
        assets.forEach { asset ->
            when (asset.assetType) {
                "COVER", "CHARACTER_POSTER" -> ImageAssetCard(asset = asset)
                "COMIC_PANEL" -> ComicAssetCard(asset = asset)
                "VIDEO" -> VideoAssetCard(asset = asset)
                "TEXT", "CHAPTER", "STORY_CARD", "VIDEO_SCRIPT" -> TextAssetCard(asset = asset)
                else -> GenericAssetCard(asset = asset)
            }
        }
        if (assets.none { it.assetType == "VIDEO" } && work.videoUrl.isNotBlank()) {
            VideoAssetCard(
                asset = WorkAsset(
                    assetId = "video_${work.workId}",
                    workId = work.workId,
                    projectId = work.projectId,
                    outputKind = work.outputKind,
                    assetType = "VIDEO",
                    orderIndex = assets.size,
                    title = "短视频成品",
                    summary = "后端返回的视频资源。",
                    url = work.videoUrl,
                    textContent = "",
                ),
            )
        }
    }
}

@Composable
private fun ImageAssetCard(asset: WorkAsset) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = asset.title, style = MaterialTheme.typography.titleSmall)
        if (asset.url.isNotBlank()) {
            RemoteCoverImage(imageUrl = asset.url, title = asset.title)
        }
        Text(text = asset.summary, color = WeUiTextSecondary)
    }
}

@Composable
private fun ComicAssetCard(asset: WorkAsset) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WeUiBackgroundMuted, RoundedCornerShape(12.dp))
            .border(1.dp, WeUiBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (asset.url.isNotBlank()) {
            RemoteCoverImage(imageUrl = asset.url, title = asset.title)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.45f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(WeUiSurface),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "第 ${asset.orderIndex + 1} 格", color = WeUiTextSecondary)
            }
        }
        Text(text = asset.title, style = MaterialTheme.typography.titleSmall)
        Text(text = asset.summary)
        if (asset.textContent.isNotBlank()) {
            Text(text = asset.textContent, color = WeUiTextSecondary)
        }
    }
}

@Composable
private fun VideoAssetCard(asset: WorkAsset) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = asset.title, style = MaterialTheme.typography.titleSmall)
        if (asset.url.isNotBlank()) {
            RemoteVideoPlayer(videoUrl = asset.url, title = asset.title)
        } else {
            Text(text = "视频仍在生成或暂未返回地址，已保留脚本。", color = WeUiTextSecondary)
        }
        Text(text = asset.summary, color = WeUiTextSecondary)
        if (asset.textContent.isNotBlank()) {
            TextAssetContent(text = asset.textContent)
        }
    }
}

@Composable
private fun TextAssetCard(asset: WorkAsset) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WeUiBackgroundMuted, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = asset.title, style = MaterialTheme.typography.titleSmall)
        Text(text = asset.summary, color = WeUiTextSecondary)
        TextAssetContent(text = asset.textContent)
    }
}

@Composable
private fun GenericAssetCard(asset: WorkAsset) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = asset.title, style = MaterialTheme.typography.titleSmall)
        Text(text = asset.summary)
        if (asset.url.isNotBlank()) {
            SelectionContainer {
                Text(text = asset.url, color = WeUiTextSecondary)
            }
        }
        if (asset.textContent.isNotBlank()) {
            TextAssetContent(text = asset.textContent)
        }
    }
}

@Composable
private fun TextAssetContent(text: String) {
    if (text.isBlank()) return
    SelectionContainer {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComicStoryboardBlock(storyboard: List<StoryboardSection>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WeUiSurface, RoundedCornerShape(18.dp))
            .border(1.dp, WeUiBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "连环漫画分镜", style = MaterialTheme.typography.titleMedium)
        if (storyboard.isEmpty()) {
            Text(
                text = "正在读取分镜产出；如果一直为空，请重新进入作品详情或重新导出一次漫画分镜包。",
                color = WeUiTextSecondary,
            )
            return@Column
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            storyboard.forEachIndexed { index, section ->
                ComicPanel(index = index, section = section)
            }
        }
    }
}

@Composable
private fun ComicPanel(
    index: Int,
    section: StoryboardSection,
) {
    Column(
        modifier = Modifier
            .widthIn(min = 142.dp)
            .fillMaxWidth()
            .background(WeUiBackgroundMuted, RoundedCornerShape(12.dp))
            .border(1.dp, WeUiBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(if (index % 4 == 0 || index % 4 == 3) 2.1f else 1.15f)
                .clip(RoundedCornerShape(8.dp))
                .background(WeUiSurface),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "第 ${index + 1} 格", color = WeUiTextSecondary)
        }
        Text(text = section.title, style = MaterialTheme.typography.titleSmall)
        Text(text = section.summary)
        Text(text = section.subtitleLine, color = WeUiTextSecondary)
        InfoPill(text = section.durationLabel)
    }
}

@Composable
private fun AssetBlock(
    title: String,
    value: String,
    emptyText: String,
    preview: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WeUiSurface, RoundedCornerShape(18.dp))
            .border(1.dp, WeUiBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        if (value.isNotBlank()) {
            preview()
        }
        SelectionContainer {
            Text(
                text = value.ifBlank { emptyText },
                style = MaterialTheme.typography.bodyMedium,
                color = WeUiTextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun Work.isComicWork(): Boolean {
    return outputKind == "COMIC_STORYBOARD" ||
        sceneLabel.contains("漫画") ||
        sceneLabel.contains("连环") ||
        durationLabel.contains("格漫画") ||
        title.contains("连环漫画")
}

private fun Work.isVideoWork(): Boolean {
    return outputKind == "SHORT_VIDEO" ||
        videoUrl.isNotBlank() ||
        sceneLabel.contains("视频") ||
        durationLabel.contains("视频")
}

@Composable
private fun RemoteCoverImage(
    imageUrl: String,
    title: String,
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(14.dp))
            .background(WeUiBackgroundMuted),
        contentAlignment = Alignment.Center,
    ) {
        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(text = "封面载入中", color = WeUiTextSecondary)
        }
    }
}

@Composable
private fun RemoteVideoPlayer(
    videoUrl: String,
    title: String,
) {
    if (videoUrl.isBlank()) {
        return
    }

    val context = LocalContext.current
    var videoView by remember(videoUrl) { mutableStateOf<VideoView?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(14.dp))
            .background(WeUiBackgroundMuted),
        contentAlignment = Alignment.Center,
    ) {
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
    }

    DisposableEffect(videoUrl) {
        onDispose {
            videoView?.stopPlayback()
        }
    }
}
