package com.yingrensheng.core.model.member

data class MemberPlan(
    val planId: String,
    val name: String,
    val monthlyPrice: Int,
    val badge: String?,
    val summary: String,
    val features: List<String>,
)
