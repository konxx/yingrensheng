package com.yingrensheng.core.model.member

data class MemberInfo(
    val levelName: String,
    val subtitle: String,
    val benefits: List<String>,
    val highlightLabel: String,
    val activePlanPriceLabel: String,
)

