package com.yingrensheng.core.network

object YrsApiConfig {
    // USB device debugging uses adb reverse: adb reverse tcp:3000 tcp:3000.
    // The app module can override this at startup for emulator or LAN debugging.
    @Volatile
    private var backendOriginValue: String = "http://127.0.0.1:3000"

    val BackendOrigin: String
        get() = backendOriginValue

    val DefaultBaseUrl: String
        get() = "$BackendOrigin/api/v1"

    val HealthUrl: String
        get() = "$BackendOrigin/health"

    fun configureBackendOrigin(origin: String) {
        backendOriginValue = origin.trimEnd('/')
    }

    fun api(path: String): String {
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        return DefaultBaseUrl.trimEnd('/') + normalizedPath
    }

    fun asset(path: String): String {
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        return BackendOrigin.trimEnd('/') + normalizedPath
    }
}
