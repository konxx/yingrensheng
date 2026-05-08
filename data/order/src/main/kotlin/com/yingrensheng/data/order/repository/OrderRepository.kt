package com.yingrensheng.data.order.repository

import com.yingrensheng.core.model.order.Order
import java.time.Instant

interface OrderRepository {
    fun getOrders(): List<Order>
}

object OrderRepositoryProvider {
    @Volatile
    var current: OrderRepository = FakeOrderRepository()
}

class FakeOrderRepository : OrderRepository {
    override fun getOrders(): List<Order> {
        return listOf(
            Order(
                orderId = "order_20260508_01",
                title = "大理五月风 导出",
                amountLabel = "¥39.90",
                exportSpec = "1080P 无水印",
                statusLabel = "已支付",
                createdAt = Instant.now().minusSeconds(86400),
            ),
            Order(
                orderId = "order_20260507_02",
                title = "会员年卡",
                amountLabel = "¥168.00",
                exportSpec = "会员权益",
                statusLabel = "已生效",
                createdAt = Instant.now().minusSeconds(172800),
            ),
        )
    }
}
