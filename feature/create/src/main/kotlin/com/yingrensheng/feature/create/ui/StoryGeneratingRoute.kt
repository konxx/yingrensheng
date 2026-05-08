package com.yingrensheng.feature.create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@Composable
fun StoryGeneratingRoute(
    onContinue: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    val session by creationRepository.observeSession().collectAsState()
    val task = session.renderTask

    YrsScaffold(
        title = "AI 正在编排故事板",
        subtitle = "真实版本这里会接异步任务和轮询；当前原型先把关键状态结构跑通。",
    ) {
        if (task == null) {
            YrsPrimaryButton(
                text = "开始编排",
                onClick = { creationRepository.generateStoryDraft() },
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
                text = "查看故事板",
                onClick = {
                    creationRepository.buildStoryboard()
                    onContinue()
                },
            )
        }
    }
}
