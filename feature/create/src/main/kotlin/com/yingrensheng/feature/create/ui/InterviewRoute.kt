package com.yingrensheng.feature.create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@Composable
fun InterviewRoute(
    onContinue: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    val answers = remember { mutableStateMapOf<String, String>() }
    val prompts = creationRepository.interviewPrompts()

    YrsScaffold(
        title = "让 AI 先理解你想表达什么",
        subtitle = "这一页故意做得像轻访谈，而不是冷冰冰的表单。",
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
            text = "保存回答",
            onClick = {
                answers.forEach { (key, value) ->
                    creationRepository.answerPrompt(key, value)
                }
                onContinue()
            },
        )
    }
}
