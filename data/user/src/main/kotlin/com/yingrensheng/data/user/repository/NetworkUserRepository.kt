package com.yingrensheng.data.user.repository

import com.google.gson.reflect.TypeToken
import com.yingrensheng.core.model.user.User
import com.yingrensheng.core.model.user.UserSession
import com.yingrensheng.core.network.NetworkApiResponse
import com.yingrensheng.core.network.SimpleApiClient
import com.yingrensheng.core.network.YrsApiConfig
import com.yingrensheng.core.network.requireData
import java.net.HttpURLConnection
import java.net.URL
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

    override fun isBackendReachable(): Boolean {
        return runCatching {
            val connection = (URL("http://10.0.2.2:3000/health").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 2_000
                readTimeout = 2_000
            }
            val ok = connection.responseCode in 200..299
            connection.disconnect()
            ok
        }.getOrDefault(false)
    }

    override fun acceptAgreement() {
        sessionState.value = sessionState.value.copy(hasAcceptedAgreement = true)
    }

    override fun login(username: String, password: String) {
        runCatching {
            val loginType = object : TypeToken<NetworkApiResponse<LoginResponse>>() {}.type
            val envelope: NetworkApiResponse<LoginResponse> = apiClient.post(
                path = "/auth/login",
                body = mapOf(
                    "username" to username,
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
                    username = response.user.username,
                    email = response.user.email,
                    nickname = response.user.nickname,
                    avatarLabel = response.user.nickname.take(2),
                ),
            )
        }.onFailure {
            fallback.acceptAgreement()
            fallback.login(username, password)
            sessionState.value = fallback.session().value
        }
    }

    override fun register(username: String, nickname: String, email: String, password: String) {
        runCatching {
            val registerType = object : TypeToken<NetworkApiResponse<LoginResponse>>() {}.type
            val envelope: NetworkApiResponse<LoginResponse> = apiClient.post(
                path = "/auth/register",
                body = mapOf(
                    "username" to username,
                    "nickname" to nickname,
                    "email" to email,
                    "password" to password,
                ),
                type = registerType,
            )
            val response = envelope.requireData()
            sessionState.value = sessionState.value.copy(
                user = User(
                    userId = response.user.userId,
                    username = response.user.username,
                    email = response.user.email,
                    nickname = response.user.nickname,
                    avatarLabel = response.user.nickname.take(2),
                ),
            )
        }.onFailure {
            fallback.acceptAgreement()
            fallback.register(username = username, nickname = nickname, email = email, password = password)
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
    val username: String,
    val email: String,
    val nickname: String,
    val avatarUrl: String,
)
