package com.yingrensheng.feature.create.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider
import kotlinx.coroutines.launch

@Composable
fun MaterialImportRoute(
    onContinue: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val creationRepository = CreationRepositoryProvider.current
    val session by creationRepository.observeSession().collectAsState()
    val uploader = remember { MaterialUploadHelper() }
    val sceneId = session.selectedScene?.sceneId.orEmpty()
    var themeLine by remember(sceneId) {
        mutableStateOf(
            session.themeLine.ifBlank {
                defaultInput(sceneId)
            },
        )
    }
    var uploading by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20),
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        val projectId = creationRepository.observeSession().value.currentProjectId ?: return@rememberLauncherForActivityResult
        scope.launch {
            uploading = true
            statusText = null
            runCatching {
                val materials = uploader.uploadSelectedMedia(
                    context = context,
                    projectId = projectId,
                    uris = uris,
                )
                creationRepository.replaceMaterials(materials)
                onContinue()
            }.onFailure {
                statusText = it.message ?: "素材上传失败"
            }
            uploading = false
        }
    }

    YrsScaffold(
        title = inputTitle(sceneId),
        subtitle = inputSubtitle(sceneId),
    ) {
        YrsSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = inputHelper(sceneId))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = themeLine,
                    onValueChange = {
                        themeLine = it
                        creationRepository.updateThemeLine(it)
                    },
                    minLines = if (sceneId.startsWith("scene_character")) 3 else 7,
                    label = {
                        Text(inputLabel(sceneId))
                    },
                )
                if (sceneId.startsWith("scene_character")) {
                    YrsPrimaryButton(
                        text = if (uploading) "正在上传照片..." else photoButtonText(sceneId),
                        enabled = !uploading,
                        onClick = {
                            creationRepository.updateThemeLine(themeLine)
                            pickMediaLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                    )
                    YrsPrimaryButton(
                        text = sampleButtonText(sceneId),
                        enabled = !uploading,
                        onClick = {
                            creationRepository.updateThemeLine(themeLine)
                            creationRepository.importMaterials()
                            onContinue()
                        },
                    )
                } else {
                    YrsPrimaryButton(
                        text = continueButtonText(sceneId),
                        onClick = {
                            creationRepository.updateThemeLine(themeLine)
                            creationRepository.importMaterials()
                            onContinue()
                        },
                    )
                }
                if (statusText != null) {
                    Text(text = statusText!!)
                }
            }
        }
    }
}

private fun inputTitle(sceneId: String): String {
    return when (sceneId) {
        "scene_character_xiyou" -> "上传照片，定西游身份"
        "scene_character_honglou" -> "上传照片，入红楼关系网"
        "scene_outline_history" -> "输入大纲，指定融合方向"
        "scene_outline_original" -> "输入灵感，搭原创世界"
        "scene_media_comic" -> "粘贴小说，拆成连环画"
        "scene_media_short_video" -> "粘贴小说，拆成短视频"
        else -> "准备创作输入"
    }
}

private fun inputSubtitle(sceneId: String): String {
    return when (sceneId) {
        "scene_character_xiyou" -> "只需要一张清晰人物照和一句身份愿望，直接生成取经路上的角色故事。"
        "scene_character_honglou" -> "红楼更重人物气质和关系位置，上传后会先复核照片再补设定。"
        "scene_outline_history" -> "这里要说明历史背景、名著/朝代参照和主线冲突，下一步直接选文风。"
        "scene_outline_original" -> "不用先想朝代模板，先说人物欲望、世界规则和你想要的结局感。"
        "scene_media_comic" -> "漫画流程会跳过访谈和风格页，直接把小说拆成格子画面。"
        "scene_media_short_video" -> "短视频流程会保留镜头风格选择，后面进入首帧和视频预览。"
        else -> "把 AI 需要理解的核心信息先放进来。"
    }
}

private fun inputHelper(sceneId: String): String {
    return when (sceneId) {
        "scene_character_xiyou" -> "建议写清楚想成为唐僧、悟空同路人、女儿国来客，或一个全新的取经角色。"
        "scene_character_honglou" -> "建议写清楚人物在贾府的位置、与宝黛钗的关系、想保留照片里的哪种气质。"
        "scene_outline_history" -> "可以写人物、冲突、朝代、名著参照和结局方向；AI 会把它融合成历史小说结构。"
        "scene_outline_original" -> "可以只写一句灵感，也可以写世界观、主角目标、反派阻力和最终情绪。"
        "scene_media_comic" -> "粘贴小说正文或关键章节，漫画会优先提取场景、动作、台词和分格节奏。"
        "scene_media_short_video" -> "粘贴小说正文或爆点片段，短视频会优先提取前 5 秒钩子、旁白和镜头。"
        else -> "当前版本先记录输入并进入生成链路。"
    }
}

private fun defaultInput(sceneId: String): String {
    return when (sceneId) {
        "scene_character_xiyou" -> "我想把照片里的主角架空成西游记中的取经人，温和坚定，但身上藏着一段未被天命写完的因果。"
        "scene_character_honglou" -> "我想让照片里的主角成为贾府外来的贵公子，气质清雅，和宝玉、黛玉有一段若即若离的旧缘。"
        "scene_outline_history" -> "一个现代女孩进入红楼梦世界，成为贾府外来人物，并试图改变林黛玉的命运。"
        "scene_outline_original" -> "一个能听见古画低语的修书人，在修复一幅残卷时发现自己的身世被藏在画中。"
        "scene_media_comic" -> "粘贴小说正文或关键片段，AI 会拆成 6-12 格连环漫画：每格包含画面、人物动作、对白和字幕。"
        "scene_media_short_video" -> "粘贴小说正文或关键片段，AI 会拆成短视频镜头：前 5 秒钩子、旁白、字幕、首帧和镜头运动。"
        else -> "写下这次创作最重要的一句话。"
    }
}

private fun inputLabel(sceneId: String): String {
    return when (sceneId) {
        "scene_character_xiyou" -> "西游身份愿望"
        "scene_character_honglou" -> "红楼身份与关系"
        "scene_outline_history" -> "历史/名著融合大纲"
        "scene_outline_original" -> "原创故事灵感"
        "scene_media_comic" -> "小说正文或章节"
        "scene_media_short_video" -> "小说正文或爆点片段"
        else -> "创作说明"
    }
}

private fun photoButtonText(sceneId: String): String {
    return when (sceneId) {
        "scene_character_xiyou" -> "选择照片生成西游角色"
        "scene_character_honglou" -> "选择照片生成红楼角色"
        else -> "从相册选择人物照片"
    }
}

private fun sampleButtonText(sceneId: String): String {
    return when (sceneId) {
        "scene_character_xiyou" -> "先用推荐人物走西游流程"
        "scene_character_honglou" -> "先用推荐人物走红楼流程"
        else -> "先用推荐人物继续"
    }
}

private fun continueButtonText(sceneId: String): String {
    return when (sceneId) {
        "scene_outline_history" -> "保存大纲，选择历史文风"
        "scene_outline_original" -> "保存灵感，补原创设定"
        "scene_media_comic" -> "保存小说，直接拆漫画"
        "scene_media_short_video" -> "保存小说，选择镜头风格"
        else -> "保存输入并继续"
    }
}
