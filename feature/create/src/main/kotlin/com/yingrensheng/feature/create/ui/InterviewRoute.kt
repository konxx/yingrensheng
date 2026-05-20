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
import com.yingrensheng.core.model.creation.InterviewPrompt
import com.yingrensheng.core.model.project.CreationMode
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@Composable
fun InterviewRoute(
    onContinue: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    val session by creationRepository.observeSession().collectAsState()
    val sceneId = session.selectedScene?.sceneId.orEmpty()
    val answers = remember(sceneId) { mutableStateMapOf<String, String>() }
    val prompts = sceneInterviewPrompts(sceneId).ifEmpty { creationRepository.interviewPrompts() }

    YrsScaffold(
        title = interviewTitle(sceneId),
        subtitle = interviewSubtitle(sceneId, session.mode),
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

private fun sceneInterviewPrompts(sceneId: String): List<InterviewPrompt> {
    return when (sceneId) {
        "scene_character_honglou" -> listOf(
            InterviewPrompt("prompt_1", "主角进贾府后的身份是什么？", "例如宝玉旧友、贾母远亲、寄居大观园的新公子。"),
            InterviewPrompt("prompt_2", "最想强化照片里的哪种气质？", "例如清贵、病弱、疏离、少年感、温润或锋利。"),
            InterviewPrompt("prompt_3", "和宝玉、黛玉、宝钗的关系要怎样？", "这会决定人物关系网和第一段剧情冲突。"),
        )
        "scene_outline_original" -> listOf(
            InterviewPrompt("prompt_1", "主角真正想得到什么？", "用一句话写清楚欲望，越具体越好。"),
            InterviewPrompt("prompt_2", "这个世界最特别的规则是什么？", "例如古画会说话、命簿可改、梦境能交换寿命。"),
            InterviewPrompt("prompt_3", "结尾希望留下哪种感觉？", "爽感、余韵、悬疑、温柔、反转或悲剧美感。"),
        )
        else -> emptyList()
    }
}

private fun interviewTitle(sceneId: String): String {
    return when (sceneId) {
        "scene_character_honglou" -> "补红楼人物关系"
        "scene_outline_original" -> "补原创故事内核"
        else -> "让 AI 导演补齐设定"
    }
}

private fun interviewSubtitle(sceneId: String, mode: CreationMode?): String {
    return when (sceneId) {
        "scene_character_honglou" -> "红楼流程只追问身份、气质和关系网，回答后直接选叙事风格。"
        "scene_outline_original" -> "原创故事不问历史模板，重点补主角欲望、世界规则和结尾余味。"
        else -> when (mode) {
            CreationMode.CHARACTER_TIME_TRAVEL -> "把身份、气质和角色边界说清楚，后面生成角色照和剧情才会稳定。"
            CreationMode.OUTLINE_STORY -> "先把冲突、时代和结局方向讲明白，再扩写正文。"
            CreationMode.NOVEL_TO_MEDIA -> "先确定输出重点，后面拆漫画和短视频时会更贴近你的用途。"
            else -> "补充几个关键问题，让 AI 更理解你要做什么。"
        }
    }
}
