package com.yingrensheng.feature.create.ui

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@Composable
fun MaterialImportRoute(
    onContinue: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    val session by creationRepository.observeSession().collectAsState()
    var themeLine by remember { mutableStateOf(session.themeLine.ifBlank { "想把那天的晚风和笑声留下来" }) }

    YrsScaffold(
        title = "先把素材放进来",
        subtitle = "MVP 先用样例素材模拟相册、多段视频、录音和一句话主题导入。",
    ) {
        YrsSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "支持照片、视频、录音与一句话主题。")
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
                    text = "导入样例素材",
                    onClick = {
                        creationRepository.updateThemeLine(themeLine)
                        creationRepository.importMaterials()
                        onContinue()
                    },
                )
            }
        }
    }
}
