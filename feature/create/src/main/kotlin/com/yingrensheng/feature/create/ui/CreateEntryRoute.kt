package com.yingrensheng.feature.create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.model.project.CreationMode
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@Composable
fun CreateEntryRoute(
    onModeSelected: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    YrsScaffold(
        title = "选择创作方式",
        subtitle = "先定输入方式：照片造角色、大纲写小说，或把已有小说改成漫画和短视频。",
    ) {
        creationRepository.creationModes().forEach { (mode, label) ->
            val subtitle = when (mode) {
                CreationMode.CHARACTER_TIME_TRAVEL -> "上传自拍或人物照，生成西游记、红楼梦等历史架空角色"
                CreationMode.OUTLINE_STORY -> "输入一段大纲，融合历史小说或扩写成原创故事"
                CreationMode.NOVEL_TO_MEDIA -> "粘贴小说，生成连环漫画分镜或短视频脚本"
                CreationMode.QUICK_FILM -> "旧版快速成片入口"
                CreationMode.STORY_FILM -> "旧版故事成片入口"
            }
            YrsSurfaceCard(
                modifier = androidx.compose.ui.Modifier,
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = label, style = MaterialTheme.typography.titleLarge)
                        Text(text = subtitle)
                        com.yingrensheng.core.designsystem.component.YrsPrimaryButton(
                            text = "进入$label",
                            onClick = {
                                creationRepository.selectMode(mode)
                                onModeSelected()
                            },
                        )
                    }
                },
            )
        }
    }
}
