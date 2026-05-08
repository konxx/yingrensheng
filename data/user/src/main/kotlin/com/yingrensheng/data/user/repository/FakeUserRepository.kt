package com.yingrensheng.data.user.repository

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

    override fun acceptAgreement() {
        sessionState.value = sessionState.value.copy(hasAcceptedAgreement = true)
    }

    override fun login(phone: String, password: String) {
        val nickname = if (phone == "admin") "Administrator" else "林青"
        sessionState.value = sessionState.value.copy(
            user = User(
                userId = if (phone == "admin") "admin_001" else "user_001",
                nickname = nickname,
                phone = phone,
                avatarLabel = nickname.take(2),
            ),
        )
    }

    override fun register(phone: String, nickname: String, password: String) {
        sessionState.value = sessionState.value.copy(
            user = User(
                userId = "user_fake_register",
                nickname = nickname,
                phone = phone,
                avatarLabel = nickname.take(2),
            ),
        )
    }
}
