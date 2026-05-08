package com.yingrensheng.data.user.repository

import com.google.gson.reflect.TypeToken
import com.yingrensheng.core.model.user.User
import com.yingrensheng.core.model.user.UserSession
import com.yingrensheng.core.network.NetworkApiResponse
import com.yingrensheng.core.network.SimpleApiClient
import com.yingrensheng.core.network.YrsApiConfig
import com.yingrensheng.core.network.requireData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkUserRepository(
    private val apiClient: SimpleApiClient,
    private val fallback: UserRepository,
) : UserRepository {
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
        runCatching {
            val loginType = object : TypeToken<NetworkApiResponse<LoginResponse>>() {}.type
            val envelope: NetworkApiResponse<LoginResponse> = apiClient.post(
                path = "/auth/login",
                body = mapOf(
                    "phone" to phone,
                    "smsCode" to "123456",
                    "deviceId" to "android_local",
                    "password" to password,
                ),
                type = loginType,
            )
            val response = envelope.requireData()
            sessionState.value = sessionState.value.copy(
                user = User(
                    userId = response.user.userId,
                    nickname = response.user.nickname,
                    phone = response.user.phone,
                    avatarLabel = response.user.nickname.take(2),
                ),
            )
        }.onFailure {
            fallback.acceptAgreement()
            fallback.login(phone, password)
            sessionState.value = fallback.session().value
        }
    }

    override fun register(phone: String, nickname: String, password: String) {
        runCatching {
            val registerType = object : TypeToken<NetworkApiResponse<LoginResponse>>() {}.type
            val envelope: NetworkApiResponse<LoginResponse> = apiClient.post(
                path = "/auth/register",
                body = mapOf(
                    "phone" to phone,
                    "nickname" to nickname,
                    "password" to password,
                ),
                type = registerType,
            )
            val response = envelope.requireData()
            sessionState.value = sessionState.value.copy(
                user = User(
                    userId = response.user.userId,
                    nickname = response.user.nickname,
                    phone = response.user.phone,
                    avatarLabel = response.user.nickname.take(2),
                ),
            )
        }.onFailure {
            fallback.acceptAgreement()
            fallback.register(phone = phone, nickname = nickname, password = password)
            sessionState.value = fallback.session().value
        }
    }

    companion object {
        fun fallbackAware(): UserRepository {
            val fake = FakeUserRepository()
            return NetworkUserRepository(
                apiClient = SimpleApiClient(YrsApiConfig.DefaultBaseUrl),
                fallback = fake,
            )
        }
    }
}

private data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Int,
    val user: LoginUser,
)

private data class LoginUser(
    val userId: String,
    val nickname: String,
    val phone: String,
    val avatarUrl: String,
)
