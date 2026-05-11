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
    var themeLine by remember { mutableStateOf(session.themeLine.ifBlank { "想把那天的晚风和笑声留下来" }) }
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
        title = "先把素材放进来",
        subtitle = "现在已经支持从系统相册真实选择图片和视频，并直接上传到后端。",
    ) {
        YrsSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "当前已支持图片 + 视频导入。音频后续再补。")
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = themeLine,
                    onValueChange = {
                        themeLine = it
                        creationRepository.updateThemeLine(it)
                    },
                    label = { Text("一句话主题") },
                )
                YrsPrimaryButton(
                    text = if (uploading) "正在上传素材..." else "从相册选择图片和视频",
                    enabled = !uploading,
                    onClick = {
                        creationRepository.updateThemeLine(themeLine)
                        pickMediaLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo),
                        )
                    },
                )
                if (statusText != null) {
                    Text(text = statusText!!)
                }
            }
        }
    }
}
