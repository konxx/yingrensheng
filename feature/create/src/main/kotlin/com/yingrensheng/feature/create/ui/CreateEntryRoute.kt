package com.yingrensheng.feature.create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.model.creation.SceneTemplate
import com.yingrensheng.core.model.project.CreationMode
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateEntryRoute(
    onFlowStarted: (SceneTemplate) -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    YrsScaffold(
        title = "选择要生成什么",
        subtitle = "六种创作各走自己的短流程，不再把照片、大纲、小说改编都塞进同一套步骤里。",
    ) {
        creationRepository.scenes().forEach { scene ->
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = scene.iconToken(),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = scene.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = scene.subtitle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        InfoPill(text = scene.recommendedDurationLabel)
                        InfoPill(text = scene.estimatedTimeLabel)
                        InfoPill(text = scene.flowSummary())
                    }
                    YrsPrimaryButton(
                        text = scene.startButtonText(),
                        onClick = {
                            creationRepository.selectMode(scene.creationMode())
                            creationRepository.selectScene(scene)
                            onFlowStarted(scene)
                        },
                    )
                }
            }
        }
    }
}

private fun SceneTemplate.iconToken(): String {
    return when (sceneId) {
        "scene_character_xiyou" -> "西"
        "scene_character_honglou" -> "红"
        "scene_outline_history" -> "史"
        "scene_outline_original" -> "写"
        "scene_media_comic" -> "漫"
        "scene_media_short_video" -> "影"
        else -> "创"
    }
}

private fun SceneTemplate.creationMode(): CreationMode {
    return when {
        sceneId.startsWith("scene_character") -> CreationMode.CHARACTER_TIME_TRAVEL
        sceneId.startsWith("scene_outline") -> CreationMode.OUTLINE_STORY
        sceneId.startsWith("scene_media") -> CreationMode.NOVEL_TO_MEDIA
        else -> CreationMode.OUTLINE_STORY
    }
}

private fun SceneTemplate.startButtonText(): String {
    return when (sceneId) {
        "scene_character_xiyou" -> "上传照片生成西游角色"
        "scene_character_honglou" -> "上传照片进入红楼设定"
        "scene_outline_history" -> "输入大纲融合历史小说"
        "scene_outline_original" -> "输入灵感写原创故事"
        "scene_media_comic" -> "粘贴小说生成漫画"
        "scene_media_short_video" -> "粘贴小说生成短视频"
        else -> "开始${title}"
    }
}

private fun SceneTemplate.flowSummary(): String {
    return when (sceneId) {
        "scene_character_xiyou" -> "照片 -> 草稿 -> 分镜"
        "scene_character_honglou" -> "照片 -> 复核 -> 导演设定"
        "scene_outline_history" -> "大纲 -> 风格 -> 分镜"
        "scene_outline_original" -> "灵感 -> 设定 -> 成稿"
        "scene_media_comic" -> "小说 -> 自动拆格"
        "scene_media_short_video" -> "小说 -> 镜头风格 -> 预览"
        else -> "专属流程"
    }
}
