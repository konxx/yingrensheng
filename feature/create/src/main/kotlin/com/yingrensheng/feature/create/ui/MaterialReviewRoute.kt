package com.yingrensheng.feature.create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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

@Composable
fun MaterialReviewRoute(
    onContinue: () -> Unit,
) {
    val session by CreationRepositoryProvider.current.observeSession().collectAsState()
    val isQuickFilm = session.mode == CreationMode.QUICK_FILM

    YrsScaffold(
        title = "素材已经完成上传与入库",
        subtitle = if (isQuickFilm) {
            "快速成片会在你确认素材后直接进入情绪选择，尽快生成第一版。"
        } else {
            "故事成片会在确认素材后进入访谈整理，先把想表达的主线说清楚。"
        },
    ) {
        session.materials.forEach { material ->
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = material.title, style = MaterialTheme.typography.titleLarge)
                    InfoPill(text = "${material.type.name} ${material.durationLabel}".trim())
                    Text(text = material.insight)
                }
            }
        }
        YrsPrimaryButton(
            text = if (isQuickFilm) "确认素材并继续快速成片" else "确认素材并进入故事访谈",
            onClick = onContinue,
        )
    }
}
