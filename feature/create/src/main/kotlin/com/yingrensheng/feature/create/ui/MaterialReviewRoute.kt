package com.yingrensheng.feature.create.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.model.material.MaterialItem
import com.yingrensheng.core.model.material.MaterialType
import com.yingrensheng.core.model.project.CreationMode
import com.yingrensheng.core.network.YrsApiConfig
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MaterialReviewRoute(
    onContinue: () -> Unit,
) {
    val session by CreationRepositoryProvider.current.observeSession().collectAsState()
    val mode = session.mode

    YrsScaffold(
        title = "输入已准备好",
        subtitle = when (mode) {
            CreationMode.CHARACTER_TIME_TRAVEL -> "确认人物照片和角色愿望后，AI 会继续追问身份、气质和世界观细节。"
            CreationMode.OUTLINE_STORY -> "确认大纲后，AI 会整理冲突、时代质感和故事走向。"
            CreationMode.NOVEL_TO_MEDIA -> "确认小说文本后，AI 会拆解人物、场景、情节节点和镜头形式。"
            else -> "确认输入后继续。"
        },
    ) {
        session.materials.forEach { material ->
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MaterialPreview(material = material)
                    Text(text = material.title, style = MaterialTheme.typography.titleLarge)
                    InfoPill(text = material.reviewLabel())
                    Text(text = material.insight)
                }
            }
        }
        YrsPrimaryButton(
            text = "确认并继续 AI 访谈",
            onClick = onContinue,
        )
    }
}

@Composable
private fun MaterialPreview(material: MaterialItem) {
    val previewUri = material.previewUri
    if (material.type != MaterialType.PHOTO || previewUri.isNullOrBlank()) {
        return
    }

    val context = LocalContext.current
    var bitmap by remember(previewUri) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(previewUri) {
        bitmap = withContext(Dispatchers.IO) {
            decodePreviewBitmap(context = context, previewUri = previewUri)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        val imageBitmap = bitmap
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap.asImageBitmap(),
                contentDescription = material.title,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(text = "图片预览加载中", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun decodePreviewBitmap(context: Context, previewUri: String): Bitmap? {
    return runCatching {
        when {
            previewUri.startsWith("http://") || previewUri.startsWith("https://") -> {
                URL(previewUri).openStream().use { input ->
                    android.graphics.BitmapFactory.decodeStream(input)
                }
            }

            previewUri.startsWith("/") -> {
                URL(YrsApiConfig.asset(previewUri)).openStream().use { input ->
                    android.graphics.BitmapFactory.decodeStream(input)
                }
            }

            else -> {
                context.contentResolver.openInputStream(Uri.parse(previewUri))?.use { input ->
                    android.graphics.BitmapFactory.decodeStream(input)
                }
            }
        }
    }.getOrNull()
}

private fun MaterialItem.reviewLabel(): String {
    val typeLabel = when (type) {
        MaterialType.PHOTO -> "照片素材"
        MaterialType.VIDEO -> "视频素材"
        MaterialType.AUDIO -> "音频素材"
        MaterialType.NOTE -> "文本素材"
    }
    return durationLabel.takeIf { it.isNotBlank() && it != "-" }?.let { "$typeLabel · $it" } ?: typeLabel
}
