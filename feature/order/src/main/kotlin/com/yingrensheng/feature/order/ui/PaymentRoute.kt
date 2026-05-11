package com.yingrensheng.feature.order.ui

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
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@Composable
fun PaymentRoute(
    onContinue: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    val session by creationRepository.observeSession().collectAsState()
    val selectedPlan = session.selectedExportPlan

    YrsScaffold(
        title = "确认支付与权益使用",
        subtitle = "当前是开发版假下单，但会真实创建导出任务并回写到创作状态流。",
    ) {
        selectedPlan?.let { plan ->
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = plan.title, style = MaterialTheme.typography.titleLarge)
                    Text(text = "价格 ${plan.priceLabel}")
                    plan.benefits.forEach { benefit ->
                        Text(text = "• $benefit")
                    }
                }
            }
        }
        YrsPrimaryButton(
            text = "确认支付",
            onClick = {
                creationRepository.startExport()
                onContinue()
            },
        )
    }
}
