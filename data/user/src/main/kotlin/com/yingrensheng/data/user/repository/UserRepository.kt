package com.yingrensheng.data.user.repository

import com.yingrensheng.core.model.user.UserSession
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    fun session(): StateFlow<UserSession>

    fun acceptAgreement()

    fun login(phone: String)
}

object UserRepositoryProvider {
    @Volatile
    var current: UserRepository = FakeUserRepository()
}
