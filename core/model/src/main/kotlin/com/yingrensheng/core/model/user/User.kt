package com.yingrensheng.core.model.user

data class User(
    val userId: String,
    val username: String,
    val email: String,
    val nickname: String,
    val avatarLabel: String,
    val role: String = "user",
)

data class UserSession(
    val hasAcceptedAgreement: Boolean = false,
    val user: User? = null,
)
