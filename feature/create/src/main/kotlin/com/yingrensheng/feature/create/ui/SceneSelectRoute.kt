package com.yingrensheng.feature.create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SceneSelectRoute(
    onSceneSelected: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    val session by creationRepository.observeSession().collectAsState()
    YrsScaffold(
        title = when (session.mode) {
            CreationMode.CHARACTER_TIME_TRAVEL -> "选择角色世界"
            CreationMode.OUTLINE_STORY -> "选择故事生成方向"
            CreationMode.NOVEL_TO_MEDIA -> "选择输出形式"
            else -> "选择创作模板"
        },
        subtitle = when (session.mode) {
            CreationMode.CHARACTER_TIME_TRAVEL -> "先决定用户照片要进入哪个文学或历史架空世界。"
            CreationMode.OUTLINE_STORY -> "可以把大纲融合进经典世界观，也可以生成完全原创的故事小说。"
            CreationMode.NOVEL_TO_MEDIA -> "小说会先拆成角色、场景和剧情节点，再生成漫画或短视频脚本。"
            else -> "选择一个模板继续。"
        },
    ) {
        creationRepository.scenes().filter { scene ->
            when (session.mode) {
                CreationMode.CHARACTER_TIME_TRAVEL -> scene.sceneId.startsWith("scene_character")
                CreationMode.OUTLINE_STORY -> scene.sceneId.startsWith("scene_outline")
                CreationMode.NOVEL_TO_MEDIA -> scene.sceneId.startsWith("scene_media")
                else -> true
            }
        }.forEach { scene ->
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = scene.title, style = MaterialTheme.typography.titleLarge)
                    Text(text = scene.subtitle)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoPill(text = scene.recommendedDurationLabel)
                        InfoPill(text = scene.estimatedTimeLabel)
                    }
                    YrsPrimaryButton(
                        text = "使用${scene.title}",
                        onClick = {
                            creationRepository.selectScene(scene)
                            onSceneSelected()
                        },
                    )
                }
            }
        }
    }
}
