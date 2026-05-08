package com.yingrensheng.core.network

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class SimpleApiClient(
    private val baseUrl: String,
    private val gson: Gson = Gson(),
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

    private fun <T> request(
        method: String,
        path: String,
        body: Any?,
        type: Type,
    ): T {
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        val url = URL(baseUrl.trimEnd('/') + normalizedPath)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 5_000
            readTimeout = 8_000
            setRequestProperty("Accept", "application/json")
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
                throw IllegalStateException("HTTP $statusCode ${connection.responseMessage}: $rawText")
            }

            gson.fromJson(rawText, type)
        } finally {
            connection.disconnect()
        }
    }
}

