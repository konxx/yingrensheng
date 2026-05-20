package com.yingrensheng.core.network

data class StoredAuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochMillis: Long,
    val userId: String,
    val username: String,
    val email: String,
    val nickname: String,
    val avatarLabel: String,
    val role: String,
)

interface AuthTokenStore {
    fun load(): StoredAuthSession?
    fun save(session: StoredAuthSession)
    fun clear()
}
