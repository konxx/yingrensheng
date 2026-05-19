package com.yingrensheng.data.order.repository

import com.google.gson.reflect.TypeToken
import com.yingrensheng.core.model.order.Order
import com.yingrensheng.core.network.NetworkApiResponse
import com.yingrensheng.core.network.SimpleApiClient
import com.yingrensheng.core.network.YrsApiConfig
import com.yingrensheng.core.network.requireData
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

interface OrderRepository {
    fun getOrders(): List<Order>
}

object OrderRepositoryProvider {
    @Volatile
    var current: OrderRepository = NetworkOrderRepository.fallbackAware()
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

class NetworkOrderRepository(
    private val apiClient: SimpleApiClient,
    private val fallback: OrderRepository,
) : OrderRepository {
    override fun getOrders(): List<Order> {
        return runBlocking(Dispatchers.IO) {
            runCatching {
                val type = object : TypeToken<NetworkApiResponse<List<OrderPayload>>>() {}.type
                val envelope: NetworkApiResponse<List<OrderPayload>> = apiClient.get("/orders", type)
                envelope.requireData().map {
                    Order(
                        orderId = it.orderId,
                        title = it.title,
                        amountLabel = it.amountLabel,
                        exportSpec = it.exportSpec,
                        statusLabel = it.statusLabel,
                        createdAt = runCatching { Instant.parse(it.createdAt) }.getOrDefault(Instant.now()),
                    )
                }
            }.getOrElse { fallback.getOrders() }
        }
    }

    companion object {
        fun fallbackAware(): OrderRepository {
            val fake = FakeOrderRepository()
            return NetworkOrderRepository(
                apiClient = SimpleApiClient(),
                fallback = fake,
            )
        }
    }
}

private data class OrderPayload(
    val orderId: String,
    val title: String,
    val amountLabel: String,
    val exportSpec: String,
    val statusLabel: String,
    val createdAt: String,
)
