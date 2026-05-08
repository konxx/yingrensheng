package com.yingrensheng.feature.agency.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.agency.repository.AgencyRepositoryProvider

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgencyEntryRoute() {
    val overview = AgencyRepositoryProvider.current.getAgencyOverview()

    YrsScaffold(
        title = overview.title,
        subtitle = overview.summary,
    ) {
        YrsSurfaceCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "预留能力", style = MaterialTheme.typography.titleLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    overview.capabilities.forEach { capability ->
                        InfoPill(text = capability)
                    }
                }
            }
        }
    }
}
