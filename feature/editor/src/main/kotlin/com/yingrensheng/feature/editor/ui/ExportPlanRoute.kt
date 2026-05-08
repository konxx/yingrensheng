package com.yingrensheng.feature.editor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportPlanRoute(
    onContinue: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    YrsScaffold(
        title = "这里既是导出页，也是价值确认页",
        subtitle = "用户要明确知道买到什么规格、有没有水印，以及会员多了哪些体验。",
    ) {
        creationRepository.exportPlans().forEach { plan ->
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = plan.title, style = MaterialTheme.typography.titleLarge)
                    Text(text = plan.priceLabel)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        plan.benefits.forEach { benefit ->
                            InfoPill(text = benefit)
                        }
                    }
                    YrsPrimaryButton(
                        text = "选择 ${plan.title}",
                        onClick = {
                            creationRepository.selectExportPlan(plan)
                            onContinue()
                        },
                    )
                }
            }
        }
    }
}
