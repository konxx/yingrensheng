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
import com.yingrensheng.core.model.project.CreationMode
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
    val mode = session.mode
    var themeLine by remember(mode) {
        mutableStateOf(
            session.themeLine.ifBlank {
                when (mode) {
                    CreationMode.CHARACTER_TIME_TRAVEL -> "我想把这张照片里的主角架空成西游记里的取经人，温和但有命运感。"
                    CreationMode.OUTLINE_STORY -> "一个现代女孩进入红楼梦世界，成为贾府外来人物，并试图改变林黛玉的命运。"
                    CreationMode.NOVEL_TO_MEDIA -> "粘贴小说正文或关键片段，AI 会拆成人物、场景、分镜、旁白和字幕。"
                    else -> "写下这次创作最重要的一句话。"
                }
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
        title = when (mode) {
            CreationMode.CHARACTER_TIME_TRAVEL -> "上传人物照片"
            CreationMode.OUTLINE_STORY -> "输入故事大纲"
            CreationMode.NOVEL_TO_MEDIA -> "粘贴小说正文"
            else -> "准备创作输入"
        },
        subtitle = when (mode) {
            CreationMode.CHARACTER_TIME_TRAVEL -> "建议使用清晰自拍或半身照，后续会用于角色设定、定妆照和短剧情。"
            CreationMode.OUTLINE_STORY -> "一句灵感也可以开始，AI 会继续扩写世界观、人物关系和章节结构。"
            CreationMode.NOVEL_TO_MEDIA -> "可以先放一段核心片段，MVP 会优先生成漫画/短视频分镜脚本。"
            else -> "把 AI 需要理解的核心信息先放进来。"
        },
    ) {
        YrsSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = when (mode) {
                        CreationMode.CHARACTER_TIME_TRAVEL -> "照片用于生成角色，不直接决定最终视频质量；后续接入真实 AI 后会加入肖像授权和删除机制。"
                        CreationMode.OUTLINE_STORY -> "大纲会作为创作锚点，可以写人物、冲突、结局，也可以只写一个想法。"
                        CreationMode.NOVEL_TO_MEDIA -> "小说会先拆解成镜头级结构，便于后续生成连环漫画或短视频。"
                        else -> "当前版本先记录输入并进入生成链路。"
                    },
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = themeLine,
                    onValueChange = {
                        themeLine = it
                        creationRepository.updateThemeLine(it)
                    },
                    minLines = if (mode == CreationMode.CHARACTER_TIME_TRAVEL) 3 else 6,
                    label = {
                        Text(
                            when (mode) {
                                CreationMode.CHARACTER_TIME_TRAVEL -> "角色愿望或身份说明"
                                CreationMode.OUTLINE_STORY -> "故事大纲"
                                CreationMode.NOVEL_TO_MEDIA -> "小说正文或片段"
                                else -> "创作说明"
                            },
                        )
                    },
                )
                if (mode == CreationMode.CHARACTER_TIME_TRAVEL) {
                    YrsPrimaryButton(
                        text = if (uploading) "正在上传照片..." else "从相册选择人物照片",
                        enabled = !uploading,
                        onClick = {
                            creationRepository.updateThemeLine(themeLine)
                            pickMediaLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                    )
                    YrsPrimaryButton(
                        text = "先用示例人物继续",
                        enabled = !uploading,
                        onClick = {
                            creationRepository.updateThemeLine(themeLine)
                            creationRepository.importMaterials()
                            onContinue()
                        },
                    )
                } else {
                    YrsPrimaryButton(
                        text = "保存输入并继续",
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
