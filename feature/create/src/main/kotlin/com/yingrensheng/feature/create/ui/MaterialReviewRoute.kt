package com.yingrensheng.feature.create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.model.project.CreationMode
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

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
                    Text(text = material.title, style = MaterialTheme.typography.titleLarge)
                    InfoPill(text = "${material.type.name} ${material.durationLabel}".trim())
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
