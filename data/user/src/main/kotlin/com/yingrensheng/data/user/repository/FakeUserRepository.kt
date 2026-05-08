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

    override fun login(phone: String) {
        sessionState.value = sessionState.value.copy(
            user = User(
                userId = "user_001",
                nickname = "林青",
                phone = phone,
                avatarLabel = "LQ",
            ),
        )
    }
}

