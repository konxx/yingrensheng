package com.yingrensheng.data.user.repository

import com.yingrensheng.core.model.user.UserSession
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    fun session(): StateFlow<UserSession>

    fun isBackendReachable(): Boolean

    fun acceptAgreement()

    fun login(username: String, password: String = "123456")

    fun register(username: String, nickname: String, email: String, password: String)
}

object UserRepositoryProvider {
    @Volatile
    var current: UserRepository = NetworkUserRepository.fallbackAware()
}
