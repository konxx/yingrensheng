package com.yingrensheng.core.model.user

data class User(
    val userId: String,
    val nickname: String,
    val phone: String,
    val avatarLabel: String,
)

data class UserSession(
    val hasAcceptedAgreement: Boolean = false,
    val user: User? = null,
)

