package com.yingrensheng.core.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object AuthSessionManager {
    private val refreshMutex = Mutex()
    private val gson = Gson()

    @Volatile
    private var tokenStore: AuthTokenStore? = null

    @Volatile
    private var refreshEndpoint: String = "/auth/refresh"

    @Volatile
    private var onSessionExpired: (() -> Unit)? = null

    @Volatile
    private var currentSession: StoredAuthSession? = null

    fun initialize(
        store: AuthTokenStore,
        onExpired: () -> Unit,
        refreshPath: String = "/auth/refresh",
    ) {
        tokenStore = store
        onSessionExpired = onExpired
        refreshEndpoint = refreshPath
        val restored = store.load()
        currentSession = restored
        YrsApiConfig.configureAccessToken(restored?.accessToken)
    }

    fun setOnSessionExpired(onExpired: () -> Unit) {
        onSessionExpired = onExpired
    }

    fun restoreSession(): StoredAuthSession? {
        val restored = tokenStore?.load()
        currentSession = restored
        YrsApiConfig.configureAccessToken(restored?.accessToken)
        return restored
    }

    fun saveSession(session: StoredAuthSession) {
        currentSession = session
        YrsApiConfig.configureAccessToken(session.accessToken)
        tokenStore?.save(session)
    }

    fun clearSession() {
        currentSession = null
        YrsApiConfig.configureAccessToken(null)
        tokenStore?.clear()
    }

    fun accessToken(): String? = currentSession?.accessToken

    fun session(): StoredAuthSession? = currentSession

    fun refreshIfNeededBlocking(): Boolean {
        val session = currentSession ?: return false
        val refreshWindowMillis = 60_000L
        if (session.expiresAtEpochMillis - System.currentTimeMillis() > refreshWindowMillis) {
            return true
        }
        return refreshBlocking()
    }

    fun refreshBlocking(previousAccessToken: String? = null): Boolean = runBlocking {
        refreshMutex.withLock {
            val session = currentSession ?: return@withLock false
            if (!previousAccessToken.isNullOrBlank() && session.accessToken != previousAccessToken) {
                return@withLock true
            }
            val requestType = object : TypeToken<NetworkApiResponse<RefreshResponse>>() {}.type
            val refreshed = runCatching {
                val envelope: NetworkApiResponse<RefreshResponse> = SimpleApiClient(skipAuthRefresh = true).postWithoutAuth(
                    path = refreshEndpoint,
                    body = mapOf(
                        "refreshToken" to session.refreshToken,
                        "deviceId" to "android_local",
                    ),
                    type = requestType,
                )
                envelope.requireData()
            }.getOrElse { throwable ->
                if (throwable is HttpStatusException && throwable.statusCode == 401) {
                    clearSession()
                    onSessionExpired?.invoke()
                }
                return@withLock false
            }
            saveSession(
                StoredAuthSession(
                    accessToken = refreshed.accessToken,
                    refreshToken = refreshed.refreshToken,
                    expiresAtEpochMillis = System.currentTimeMillis() + refreshed.expiresInSeconds * 1000L,
                    userId = refreshed.user.userId,
                    username = refreshed.user.username,
                    email = refreshed.user.email,
                    nickname = refreshed.user.nickname,
                    avatarLabel = refreshed.user.nickname.take(2),
                    role = refreshed.user.role,
                ),
            )
            true
        }
    }
}

private data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Int,
    val user: RefreshUser,
)

private data class RefreshUser(
    val userId: String,
    val username: String,
    val email: String,
    val nickname: String,
    val avatarUrl: String,
    val role: String = "user",
)
