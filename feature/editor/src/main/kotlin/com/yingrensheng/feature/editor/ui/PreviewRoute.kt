package com.yingrensheng.feature.editor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.designsystem.theme.FilmPaperDeep
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreviewRoute(
    onContinue: () -> Unit,
) {
    val session by CreationRepositoryProvider.current.observeSession().collectAsState()
    val preview = session.previewAsset

    YrsScaffold(
        title = "首版成片预览",
        subtitle = "现在先围绕高价值轻编辑来设计，而不是一上来就做重型多轨。",
    ) {
        if (preview != null) {
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(FilmPaperDeep),
                    ) {
                        Text(
                            text = "Preview Frame",
                            modifier = Modifier.align(androidx.compose.ui.Alignment.Center),
                        )
                    }
                    Text(text = preview.title, style = MaterialTheme.typography.titleLarge)
                    Text(text = preview.subtitleSummary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoPill(text = preview.musicLabel)
                        InfoPill(text = preview.coverCaption)
                        InfoPill(text = "支持：换音乐 / 字幕 / 封面")
                    }
                }
            }
            YrsPrimaryButton(
                text = "选择导出方案",
                onClick = onContinue,
            )
        }
    }
}
