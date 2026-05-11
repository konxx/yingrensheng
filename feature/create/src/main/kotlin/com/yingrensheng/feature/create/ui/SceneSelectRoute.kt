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
    val isQuickFilm = session.mode == CreationMode.QUICK_FILM
    YrsScaffold(
        title = if (isQuickFilm) "快速成片先挑一个场景" else "故事成片先定一个场景",
        subtitle = if (isQuickFilm) {
            "这一条会优先追求尽快出首版，上传素材后直接进入情绪与预览生成。"
        } else {
            "这一条会保留完整访谈与故事整理流程，更适合做情绪线更完整的作品。"
        },
    ) {
        creationRepository.scenes().forEach { scene ->
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = scene.title, style = MaterialTheme.typography.titleLarge)
                    Text(text = scene.subtitle)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoPill(text = scene.recommendedDurationLabel)
                        InfoPill(text = scene.estimatedTimeLabel)
                    }
                    YrsPrimaryButton(
                        text = if (isQuickFilm) "用 ${scene.title} 快速开拍" else "用 ${scene.title} 进入导演模式",
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
