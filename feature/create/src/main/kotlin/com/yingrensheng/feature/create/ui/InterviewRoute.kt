package com.yingrensheng.feature.create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.model.project.CreationMode
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@Composable
fun InterviewRoute(
    onContinue: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    val session by creationRepository.observeSession().collectAsState()
    val answers = remember { mutableStateMapOf<String, String>() }
    val prompts = creationRepository.interviewPrompts()

    YrsScaffold(
        title = "让 AI 导演补齐设定",
        subtitle = when (session.mode) {
            CreationMode.CHARACTER_TIME_TRAVEL -> "把身份、气质和角色边界说清楚，后面生成角色照和剧情才会稳定。"
            CreationMode.OUTLINE_STORY -> "先把冲突、时代和结局方向讲明白，再扩写正文。"
            CreationMode.NOVEL_TO_MEDIA -> "先确定输出重点，后面拆漫画和短视频时会更贴近你的用途。"
            else -> "补充几个关键问题，让 AI 更理解你要做什么。"
        },
    ) {
        prompts.forEach { prompt ->
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = prompt.title)
                    Text(text = prompt.helper)
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = answers[prompt.promptId].orEmpty(),
                        onValueChange = { answers[prompt.promptId] = it },
                        label = { Text("你的回答") },
                    )
                }
            }
        }
        YrsPrimaryButton(
            text = "保存设定",
            onClick = {
                answers.forEach { (key, value) ->
                    creationRepository.answerPrompt(key, value)
                }
                onContinue()
            },
        )
    }
}
