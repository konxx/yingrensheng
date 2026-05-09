package com.yingrensheng.data.user.repository

import com.yingrensheng.core.common.result.AppResult
import com.yingrensheng.core.model.user.UserSession
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    fun session(): StateFlow<UserSession>

    fun isBackendReachable(): Boolean

    fun acceptAgreement()

    suspend fun login(username: String, password: String = "123456"): AppResult<Unit>

    suspend fun register(username: String, nickname: String, email: String, password: String): AppResult<Unit>

    suspend fun updateProfile(userId: String, username: String, nickname: String, email: String): AppResult<Unit>
}

object UserRepositoryProvider {
    @Volatile
    var current: UserRepository = NetworkUserRepository.fallbackAware()
}
