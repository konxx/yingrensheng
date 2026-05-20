package com.yingrensheng.data.user.repository

import com.google.gson.reflect.TypeToken
import com.yingrensheng.core.common.result.AppResult
import com.yingrensheng.core.model.user.User
import com.yingrensheng.core.model.user.UserSession
import com.yingrensheng.core.network.NetworkApiResponse
import com.yingrensheng.core.network.AuthSessionManager
import com.yingrensheng.core.network.SimpleApiClient
import com.yingrensheng.core.network.StoredAuthSession
import com.yingrensheng.core.network.YrsApiConfig
import com.yingrensheng.core.network.requireData
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

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
            println("YrsBackend checking ${YrsApiConfig.HealthUrl}")
            val connection = (URL(YrsApiConfig.HealthUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 2_000
                readTimeout = 2_000
            }
            val ok = connection.responseCode in 200..299
            println("YrsBackend health ${YrsApiConfig.HealthUrl} -> ${connection.responseCode}")
            connection.disconnect()
            ok
        }.getOrElse { throwable ->
            println("YrsBackend health check failed for ${YrsApiConfig.HealthUrl}: ${throwable.message}")
            false
        }
    }

    override fun acceptAgreement() {
        sessionState.value = sessionState.value.copy(hasAcceptedAgreement = true)
    }

    override suspend fun restoreSession() {
        withContext<StoredAuthSession?>(Dispatchers.IO) {
            val stored = AuthSessionManager.restoreSession() ?: return@withContext null
            if (stored.expiresAtEpochMillis <= System.currentTimeMillis()) {
                AuthSessionManager.refreshBlocking(previousAccessToken = stored.accessToken)
            }
            AuthSessionManager.session()
        }?.let { stored ->
            sessionState.value = sessionState.value.copy(
                user = User(
                    userId = stored.userId,
                    username = stored.username,
                    email = stored.email,
                    nickname = stored.nickname,
                    avatarLabel = stored.avatarLabel,
                    role = stored.role,
                ),
            )
        }
    }

    override fun logout() {
        AuthSessionManager.clearSession()
        sessionState.value = sessionState.value.copy(user = null)
    }

    override suspend fun login(username: String, password: String): AppResult<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
            val loginType = object : TypeToken<NetworkApiResponse<LoginResponse>>() {}.type
            val envelope: NetworkApiResponse<LoginResponse> = apiClient.postWithoutAuth(
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
            saveAuthSession(response)
            sessionState.value = sessionState.value.copy(
                user = User(
                    userId = response.user.userId,
                    username = response.user.username,
                    email = response.user.email,
                    nickname = response.user.nickname,
                    avatarLabel = response.user.nickname.take(2),
                    role = response.user.role,
                ),
            )
                AppResult.Success(Unit)
            }.getOrElse { throwable ->
                AppResult.Error(message = throwable.message ?: "登录失败", cause = throwable)
            }
        }
    }

    override suspend fun register(username: String, nickname: String, email: String, password: String): AppResult<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
            val registerType = object : TypeToken<NetworkApiResponse<LoginResponse>>() {}.type
            val envelope: NetworkApiResponse<LoginResponse> = apiClient.postWithoutAuth(
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
            saveAuthSession(response)
            sessionState.value = sessionState.value.copy(
                user = User(
                    userId = response.user.userId,
                    username = response.user.username,
                    email = response.user.email,
                    nickname = response.user.nickname,
                    avatarLabel = response.user.nickname.take(2),
                    role = response.user.role,
                ),
            )
                AppResult.Success(Unit)
            }.getOrElse { throwable ->
                AppResult.Error(message = throwable.message ?: "注册失败", cause = throwable)
            }
        }
    }

    private fun saveAuthSession(response: LoginResponse) {
        AuthSessionManager.saveSession(
            StoredAuthSession(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                expiresAtEpochMillis = System.currentTimeMillis() + response.expiresInSeconds * 1000L,
                userId = response.user.userId,
                username = response.user.username,
                email = response.user.email,
                nickname = response.user.nickname,
                avatarLabel = response.user.nickname.take(2),
                role = response.user.role,
            ),
        )
    }

    override suspend fun updateProfile(
        userId: String,
        username: String,
        nickname: String,
        email: String,
    ): AppResult<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val updateType = object : TypeToken<NetworkApiResponse<LoginUser>>() {}.type
                val envelope: NetworkApiResponse<LoginUser> = apiClient.post(
                    path = "/users/profile/update",
                    body = mapOf(
                        "userId" to userId,
                        "username" to username,
                        "nickname" to nickname,
                        "email" to email,
                    ),
                    type = updateType,
                )
                val response = envelope.requireData()
                sessionState.value = sessionState.value.copy(
                    user = User(
                        userId = response.userId,
                        username = response.username,
                        email = response.email,
                        nickname = response.nickname,
                        avatarLabel = response.nickname.take(2),
                        role = response.role,
                    ),
                )
                AppResult.Success(Unit)
            }.getOrElse { throwable ->
                AppResult.Error(message = throwable.message ?: "资料更新失败", cause = throwable)
            }
        }
    }

    companion object {
        fun fallbackAware(): UserRepository {
            val fake = FakeUserRepository()
            return NetworkUserRepository(
                apiClient = SimpleApiClient(),
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
    val role: String = "user",
)
