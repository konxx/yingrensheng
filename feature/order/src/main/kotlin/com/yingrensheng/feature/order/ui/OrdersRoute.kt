package com.yingrensheng.feature.order.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.yingrensheng.core.common.util.Formatters
import com.yingrensheng.core.designsystem.component.YrsSurfaceCard
import com.yingrensheng.core.ui.scaffold.YrsScaffold
import com.yingrensheng.data.order.repository.OrderRepositoryProvider

@Composable
fun OrdersRoute() {
    val orders = OrderRepositoryProvider.current.getOrders()

    YrsScaffold(
        title = "订单与导出记录",
        subtitle = "支付、导出和会员购买都从这里回看，避免单独占一个底部主导航。",
    ) {
        orders.forEach { order ->
            YrsSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = order.title, style = MaterialTheme.typography.titleLarge)
                    Text(text = "${order.amountLabel} · ${order.exportSpec}")
                    Text(text = "${order.statusLabel} · ${Formatters.formatShortTime(order.createdAt)}")
                }
            }
        }
    }
}
