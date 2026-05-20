package com.yingrensheng.core.network

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class SimpleApiClient(
    private val baseUrl: String? = null,
    private val gson: Gson = Gson(),
    private val skipAuthRefresh: Boolean = false,
) {
    fun <T> get(path: String, type: Type): T {
        return request(
            method = "GET",
            path = path,
            body = null,
            type = type,
        )
    }

    fun <T> post(path: String, body: Any?, type: Type): T {
        return request(
            method = "POST",
            path = path,
            body = body,
            type = type,
        )
    }

    fun <T> postWithoutAuth(path: String, body: Any?, type: Type): T {
        return request(
            method = "POST",
            path = path,
            body = body,
            type = type,
            withAuthorization = false,
        )
    }

    fun <T> delete(path: String, type: Type): T {
        return request(
            method = "DELETE",
            path = path,
            body = null,
            type = type,
        )
    }

    private fun <T> request(
        method: String,
        path: String,
        body: Any?,
        type: Type,
        withAuthorization: Boolean = true,
    ): T {
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        val requestBaseUrl = (baseUrl ?: YrsApiConfig.DefaultBaseUrl).trimEnd('/')
        val url = URL(requestBaseUrl + normalizedPath)
        val accessTokenBeforeRequest = AuthSessionManager.accessToken()
        return try {
            requestOnce(url, method, body, type, withAuthorization = withAuthorization)
        } catch (throwable: Throwable) {
            if (skipAuthRefresh || !withAuthorization || !throwable.isUnauthorizedHttpError()) {
                throw throwable
            }
            if (!AuthSessionManager.refreshBlocking(previousAccessToken = accessTokenBeforeRequest)) {
                throw throwable
            }
            requestOnce(url, method, body, type, withAuthorization = withAuthorization)
        }
    }

    private fun <T> requestOnce(
        url: URL,
        method: String,
        body: Any?,
        type: Type,
        withAuthorization: Boolean,
    ): T {
        if (!skipAuthRefresh && withAuthorization && YrsApiConfig.authorizationHeader() != null) {
            AuthSessionManager.refreshIfNeededBlocking()
        }
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 5_000
            readTimeout = 60_000
            setRequestProperty("Accept", "application/json")
            if (withAuthorization) YrsApiConfig.authorizationHeader()?.let { token ->
                setRequestProperty("Authorization", token)
            }
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }

        return try {
            if (body != null) {
                val payload = gson.toJson(body).toByteArray(StandardCharsets.UTF_8)
                connection.outputStream.use { output ->
                    output.write(payload)
                }
            }

            val statusCode = connection.responseCode
            val inputStream = if (statusCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val rawText = inputStream?.use { stream ->
                BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).readText()
            }.orEmpty()

            if (statusCode !in 200..299) {
                throw HttpStatusException(
                    statusCode = statusCode,
                    statusMessage = connection.responseMessage,
                    body = rawText,
                )
            }

            @Suppress("UNCHECKED_CAST")
            gson.fromJson<Any>(rawText, type) as T
        } finally {
            connection.disconnect()
        }
    }
}

class HttpStatusException(
    val statusCode: Int,
    statusMessage: String,
    body: String,
) : IllegalStateException("HTTP $statusCode $statusMessage: $body")

private fun Throwable.isUnauthorizedHttpError(): Boolean {
    return this is HttpStatusException && statusCode == 401
}
