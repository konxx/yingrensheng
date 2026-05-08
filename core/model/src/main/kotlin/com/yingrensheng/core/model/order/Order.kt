package com.yingrensheng.core.model.order

import java.time.Instant

data class Order(
    val orderId: String,
    val title: String,
    val amountLabel: String,
    val exportSpec: String,
    val statusLabel: String,
    val createdAt: Instant,
)

