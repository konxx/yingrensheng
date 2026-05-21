package com.yingrensheng.feature.member.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.designsystem.component.YrsPrimaryButton
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.designsystem.theme.WeUiGreen
import com.yingrensheng.core.designsystem.theme.WeUiLiquidGold
import com.yingrensheng.core.designsystem.theme.WeUiQr
import com.yingrensheng.core.designsystem.theme.WeUiAdminPurple
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.member.repository.MemberRepositoryProvider

private enum class BillingPeriod(
    val label: String,
    val discountTag: String?,
    val monthFactor: Int,
    val discount: Double,
    val suffix: String,
) {
    MONTHLY(label = "连续包月", discountTag = null, monthFactor = 1, discount = 1.0, suffix = "/月"),
    QUARTERLY(label = "连续包季", discountTag = "9折", monthFactor = 3, discount = 0.9, suffix = "/季"),
    YEARLY(label = "连续包年", discountTag = "8折", monthFactor = 12, discount = 0.8, suffix = "/年"),
}

private data class MemberPlan(
    val name: String,
    val monthlyPrice: Int,
    val badge: String?,
    val accent: androidx.compose.ui.graphics.Color,
    val summary: String,
    val features: List<String>,
)

@Composable
fun MemberCenterRoute() {
    var period by remember { mutableStateOf(BillingPeriod.MONTHLY) }
    val memberInfo = MemberRepositoryProvider.current.getMemberInfo()
    val isAdmin = memberInfo.levelName.hasAllAccessRights()
    val plans = MemberRepositoryProvider.current.getPlans().map {
        MemberPlan(
            name = it.name,
            monthlyPrice = it.monthlyPrice,
            badge = it.badge,
            accent = if (it.name == "Pro") WeUiQr else WeUiGreen,
            summary = it.summary,
            features = it.features,
        )
    }

    YrsScaffold(
        title = "会员权益",
        subtitle = "根据创作频率选择 Lite / Pro / Max，不同时长自动应用对应折扣。",
    ) {
        if (isAdmin) {
            AdminAccessCard(memberInfo = memberInfo)
        }

        BillingSwitcher(
            selected = period,
            onSelect = { period = it },
        )

        plans.forEach { plan ->
            val totalPrice = plan.monthlyPrice * period.monthFactor * period.discount
            val totalLabel = if (totalPrice % 1.0 == 0.0) {
                totalPrice.toInt().toString()
            } else {
                String.format("%.1f", totalPrice)
            }
            val averageMonthly = totalPrice / period.monthFactor

            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = plan.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (plan.badge != null) {
                            Text(
                                text = plan.badge,
                                modifier = Modifier
                                    .background(plan.accent.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                color = plan.accent,
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "¥$totalLabel",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = period.suffix,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Text(
                        text = when (period) {
                            BillingPeriod.MONTHLY -> "下个自然月续费金额：¥${plan.monthlyPrice}"
                            BillingPeriod.QUARTERLY -> "折后约合 ¥${String.format("%.1f", averageMonthly)}/月"
                            BillingPeriod.YEARLY -> "折后约合 ¥${String.format("%.1f", averageMonthly)}/月"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    YrsPrimaryButton(
                        text = "特惠订阅",
                        onClick = {},
                    )

                    Text(
                        text = plan.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        plan.features.forEach { feature ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "✓",
                                    color = plan.accent,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = feature,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String.hasAllAccessRights(): Boolean {
    return equals("Admin", ignoreCase = true) || contains("尊享")
}

@Composable
private fun AdminAccessCard(memberInfo: com.yingrensheng.core.model.member.MemberInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WeUiAdminPurple, RoundedCornerShape(22.dp))
            .border(1.dp, WeUiLiquidGold, RoundedCornerShape(22.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "尊享权益",
                style = MaterialTheme.typography.headlineMedium,
                color = WeUiLiquidGold,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "全部权益",
                modifier = Modifier
                    .background(WeUiLiquidGold.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                    .border(1.dp, WeUiLiquidGold.copy(alpha = 0.72f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                color = WeUiLiquidGold,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = memberInfo.subtitle,
            color = WeUiLiquidGold.copy(alpha = 0.92f),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            memberInfo.benefits.forEach { benefit ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "✓", color = WeUiLiquidGold, fontWeight = FontWeight.Bold)
                    Text(text = benefit, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.88f))
                }
            }
        }
    }
}

@Composable
private fun BillingSwitcher(
    selected: BillingPeriod,
    onSelect: (BillingPeriod) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BillingPeriod.entries.forEach { period ->
            val active = period == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (active) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .clickable { onSelect(period) }
                    .padding(vertical = 12.dp, horizontal = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = period.label,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    )
                    if (period.discountTag != null) {
                        Text(
                            text = period.discountTag,
                            color = WeUiQr,
                        )
                    }
                }
            }
        }
    }
}
