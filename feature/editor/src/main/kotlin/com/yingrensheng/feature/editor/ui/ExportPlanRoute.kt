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
import com.yingrensheng.core.model.creation.ExportPlan
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.creation.repository.CreationRepositoryProvider

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportPlanRoute(
    onContinue: () -> Unit,
    isAdmin: Boolean = false,
) {
    val creationRepository = CreationRepositoryProvider.current
    val plans = if (isAdmin) listOf(adminExportPlan()) else creationRepository.exportPlans()
    YrsScaffold(
        title = if (isAdmin) "Admin 权益导出" else "这里既是导出页，也是价值确认页",
        subtitle = if (isAdmin) {
            "系统管理员已开放全部导出权益，不消耗次数，也不需要选择单次或会员付费方案。"
        } else {
            "用户要明确知道买到什么规格、有没有水印，以及会员多了哪些体验。"
        },
    ) {
        plans.forEach { plan ->
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
                        text = if (isAdmin) "使用 Admin 权益导出" else "选择 ${plan.title}",
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

private fun adminExportPlan(): ExportPlan = ExportPlan(
    planId = "plan_admin",
    title = "系统管理员权益导出",
    priceLabel = "已豁免",
    benefits = listOf("无水印", "1080P 高清导出", "不消耗导出额度", "无需支付"),
)
