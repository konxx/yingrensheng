package com.yingrensheng.core.network

object YrsApiConfig {
    @Volatile
    private var backendOriginValue: String = "http://127.0.0.1:3000"

    @Volatile
    private var accessTokenValue: String? = null

    val BackendOrigin: String
        get() = backendOriginValue

    val DefaultBaseUrl: String
        get() = "$BackendOrigin/api/v1"

    val HealthUrl: String
        get() = "$BackendOrigin/health"

    val BackendOriginCandidates: List<String>
        get() = listOf(
            BackendOrigin,
            "http://10.0.2.2:3000",
            "http://127.0.0.1:3000",
        ).distinct()

    fun configureBackendOrigin(origin: String) {
        backendOriginValue = origin.trimEnd('/')
    }

    fun healthUrl(origin: String): String {
        return origin.trimEnd('/') + "/health"
    }

    fun configureAccessToken(token: String?) {
        accessTokenValue = token?.takeIf { it.isNotBlank() }
    }

    fun authorizationHeader(): String? {
        return accessTokenValue?.let { "Bearer $it" }
    }

    fun api(path: String): String {
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        return DefaultBaseUrl.trimEnd('/') + normalizedPath
    }

    fun asset(path: String): String {
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        return BackendOrigin.trimEnd('/') + normalizedPath
    }

    fun assetUrl(value: String): String {
        if (value.isBlank()) return ""
        if (value.startsWith("http://") || value.startsWith("https://")) return value
        if (value.startsWith("file://")) return value
        return asset(value)
    }
}
