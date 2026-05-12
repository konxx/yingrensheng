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

    YrsScaffold(
        title = "AI 正在编排改编结构",
        subtitle = "这里预留异步任务和轮询；当前先跑通草稿、分镜、预览的主链路。",
    ) {
        if (task == null) {
            YrsPrimaryButton(
                text = if (generatingDraft) "生成中" else "开始生成",
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
                text = if (generatingStoryboard) "正在生成故事板" else "查看故事板",
                onClick = {
                    if (!generatingStoryboard) {
                        scope.launch {
                            generatingStoryboard = true
                            try {
                                creationRepository.buildStoryboard()
                                onContinue()
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
