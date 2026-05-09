package com.yingrensheng.data.user.repository

import com.yingrensheng.core.common.result.AppResult
import com.yingrensheng.core.model.user.User
import com.yingrensheng.core.model.user.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeUserRepository : UserRepository {
    private val sessionState = MutableStateFlow(
        UserSession(
            hasAcceptedAgreement = false,
            user = null,
        ),
    )

    override fun session(): StateFlow<UserSession> = sessionState.asStateFlow()

    override fun isBackendReachable(): Boolean = false

    override fun acceptAgreement() {
        sessionState.value = sessionState.value.copy(hasAcceptedAgreement = true)
    }

    override suspend fun login(username: String, password: String): AppResult<Unit> {
        val nickname = if (username == "admin") "Administrator" else "林青"
        sessionState.value = sessionState.value.copy(
            user = User(
                userId = if (username == "admin") "admin_001" else "user_001",
                username = username,
                email = if (username == "admin") "admin@yingrensheng.local" else "demo@yingrensheng.local",
                nickname = nickname,
                avatarLabel = nickname.take(2),
            ),
        )
        return AppResult.Success(Unit)
    }

    override suspend fun register(username: String, nickname: String, email: String, password: String): AppResult<Unit> {
        sessionState.value = sessionState.value.copy(
            user = User(
                userId = "user_fake_register",
                username = username,
                email = email,
                nickname = nickname,
                avatarLabel = nickname.take(2),
            ),
        )
        return AppResult.Success(Unit)
    }

    override suspend fun updateProfile(
        userId: String,
        username: String,
        nickname: String,
        email: String,
    ): AppResult<Unit> {
        sessionState.value = sessionState.value.copy(
            user = User(
                userId = userId,
                username = username,
                email = email,
                nickname = nickname,
                avatarLabel = nickname.take(2),
            ),
        )
        return AppResult.Success(Unit)
    }
}
