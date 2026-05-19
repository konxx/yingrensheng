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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.yingrensheng.core.model.work.Work
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkDetailRoute(
    work: Work?,
    onDeleteWork: (Work) -> Unit = {},
) {
    var showDeleteDialog by remember(work?.workId) { mutableStateOf(false) }

    YrsScaffold(
        title = work?.title ?: "作品详情",
        subtitle = if (work == null) {
            "当前没有可展示的作品，请从作品列表重新进入。"
        } else {
            "这里展示该作品的真实导出状态、所属项目，以及可直接观看的封面和视频资源。"
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

        AssetBlock(
            title = "封面资源",
            value = work.coverUrl,
            emptyText = "暂未生成封面地址",
            preview = {
                RemoteCoverImage(
                    imageUrl = work.coverUrl,
                    title = work.title,
                )
            },
        )
        AssetBlock(
            title = "视频资源",
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
