package com.yingrensheng.feature.create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSelectableCard
import com.yingrensheng.core.model.creation.NarrativeStyle
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@Composable
fun StyleSelectRoute(
    onContinue: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    val session by creationRepository.observeSession().collectAsState()
    val sceneId = session.selectedScene?.sceneId.orEmpty()
    val styles = stylesForScene(sceneId, creationRepository.styles())
    var selectedStyle by remember(sceneId) { mutableStateOf(styles.firstOrNull()) }
    YrsScaffold(
        title = styleTitle(sceneId),
        subtitle = styleSubtitle(sceneId),
    ) {
        styles.forEach { style ->
            YrsSelectableCard(
                selected = selectedStyle?.styleId == style.styleId,
                onClick = { selectedStyle = style },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = style.title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = style.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        YrsPrimaryButton(
            text = selectedStyle?.let { "使用 ${it.title}" } ?: "选择叙事风格",
            enabled = selectedStyle != null,
            onClick = {
                selectedStyle?.let {
                    creationRepository.selectStyle(it)
                    onContinue()
                }
            },
        )
    }
}

private fun stylesForScene(sceneId: String, defaultStyles: List<NarrativeStyle>): List<NarrativeStyle> {
    return when (sceneId) {
        "scene_outline_history" -> listOf(
            NarrativeStyle("style_classic", "古典章回", "保留名著感和古风叙事，适合融合红楼、西游、明清、公案或武侠。"),
            NarrativeStyle("style_cinematic", "历史短剧", "冲突更清楚，段落更像短剧分场，适合后续转分镜。"),
            NarrativeStyle("style_webnovel", "架空爽感", "节奏更快，主角主动改局，适合架空历史和强情节。"),
        )
        "scene_media_short_video" -> listOf(
            NarrativeStyle("style_cinematic", "电影预告", "强调前 5 秒钩子、镜头推进、旁白和悬念收束。"),
            NarrativeStyle("style_webnovel", "短剧爆点", "冲突直接，字幕更抓人，适合爽文和反转剧情。"),
            NarrativeStyle("style_classic", "古风旁白", "旁白更文雅，适合名著、历史、情绪流小说。"),
        )
        "scene_character_honglou" -> listOf(
            NarrativeStyle("style_classic", "红楼雅叙", "语言更含蓄细腻，强调人物关系和情绪暗线。"),
            NarrativeStyle("style_cinematic", "园林短剧", "更适合画面化分场，突出初见、试探和冲突。"),
        )
        else -> defaultStyles
    }
}

private fun styleTitle(sceneId: String): String {
    return when (sceneId) {
        "scene_outline_history" -> "选择历史融合文风"
        "scene_media_short_video" -> "选择短视频镜头风格"
        "scene_character_honglou" -> "选择红楼叙事气质"
        else -> "选择叙事风格"
    }
}

private fun styleSubtitle(sceneId: String): String {
    return when (sceneId) {
        "scene_outline_history" -> "历史融合只提供适合小说结构的文风，不展示漫画/视频无关选项。"
        "scene_media_short_video" -> "短视频只选择镜头和旁白方向，后面会直接生成分镜与预览。"
        "scene_character_honglou" -> "红楼角色更看重人物语气和关系张力，风格候选已收窄。"
        else -> "风格会同时影响小说文风、分镜密度、旁白口吻、画面提示和封面方向。"
    }
}
