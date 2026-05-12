package com.yingrensheng.feature.editor.ui

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
fun StoryboardRoute(
    onContinue: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    val session by creationRepository.observeSession().collectAsState()
    val scope = rememberCoroutineScope()
    var generatingPreview by remember { mutableStateOf(false) }

    YrsScaffold(
        title = "漫画 / 短视频分镜",
        subtitle = "MVP 先用段落卡片承接镜头、旁白、字幕和画面提示，后续再接图片生成与视频合成。",
    ) {
        session.storyboard.forEach { section ->
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = section.title, style = MaterialTheme.typography.titleLarge)
                    Text(text = section.summary)
                    Text(text = section.subtitleLine)
                    Text(text = "片段时长 ${section.durationLabel}")
                }
            }
        }
        YrsPrimaryButton(
            text = if (generatingPreview) "正在生成预览" else "生成首版预览",
            onClick = {
                if (!generatingPreview) {
                    scope.launch {
                        generatingPreview = true
                        try {
                            creationRepository.createPreview()
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
