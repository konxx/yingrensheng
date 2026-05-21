package com.yingrensheng.feature.order.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.common.util.Formatters
import com.yingrensheng.core.designsystem.component.YrsMetricCard
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.component.InfoPill
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.order.repository.OrderRepositoryProvider

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrdersRoute() {
    val orders = OrderRepositoryProvider.current.getOrders()

    YrsScaffold(
        title = "我的订单",
        subtitle = "集中查看导出购买、会员开通与当前处理状态。",
    ) {
        YrsSurfaceCard {
            Row(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                YrsMetricCard(label = "总订单", value = orders.size.toString())
                YrsMetricCard(label = "已支付", value = orders.count { it.statusLabel.contains("已") }.toString())
                YrsMetricCard(label = "进行中", value = orders.count { it.statusLabel.contains("中") }.toString())
            }
        }

        orders.forEach { order ->
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = order.title, style = MaterialTheme.typography.titleLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoPill(text = order.amountLabel)
                    InfoPill(text = order.exportSpec)
                    InfoPill(text = order.statusLabel)
                }
                Text(
                    text = "创建时间 ${Formatters.formatShortTime(order.createdAt)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "订单号 ${order.orderId}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                }
            }
        }
    }
}
