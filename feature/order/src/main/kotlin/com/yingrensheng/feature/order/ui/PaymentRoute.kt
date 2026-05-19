package com.yingrensheng.feature.order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.designsystem.theme.WeUiAdminPurple
import com.yingrensheng.core.designsystem.theme.WeUiLiquidGold
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider
import kotlinx.coroutines.launch

@Composable
fun PaymentRoute(
    onContinue: () -> Unit,
) {
    val creationRepository = CreationRepositoryProvider.current
    val session by creationRepository.observeSession().collectAsState()
    val scope = rememberCoroutineScope()
    var exporting by remember { mutableStateOf(false) }
    val selectedPlan = session.selectedExportPlan
    val isAdminPlan = selectedPlan?.planId == "plan_admin"

    YrsScaffold(
        title = if (isAdminPlan) "确认使用 Admin 权益" else "确认支付与权益使用",
        subtitle = if (isAdminPlan) {
            "系统管理员导出会直接创建导出任务，订单记录为已豁免，不消耗会员次数。"
        } else {
            "当前是开发版假下单，但会真实创建导出任务并回写到创作状态流。"
        },
    ) {
        selectedPlan?.let { plan ->
            if (isAdminPlan) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WeUiAdminPurple, RoundedCornerShape(18.dp))
                        .border(1.dp, WeUiLiquidGold, RoundedCornerShape(18.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = WeUiLiquidGold,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(text = plan.priceLabel, color = WeUiLiquidGold)
                    plan.benefits.forEach { benefit ->
                        Text(text = "• $benefit", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.88f))
                    }
                }
            } else {
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
        }
        YrsPrimaryButton(
            text = when {
                exporting -> "正在创建导出任务"
                isAdminPlan -> "确认使用 Admin 权益"
                else -> "确认支付"
            },
            onClick = {
                if (!exporting) {
                    scope.launch {
                        exporting = true
                        try {
                            creationRepository.startExport()
                            onContinue()
                        } finally {
                            exporting = false
                        }
                    }
                }
            },
        )
    }
}
