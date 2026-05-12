package com.yingrensheng.feature.create.ui

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yingrensheng.core.model.material.MaterialItem
import com.yingrensheng.core.model.material.MaterialType
import com.yingrensheng.core.network.YrsApiConfig
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MaterialUploadHelper(
    private val gson: Gson = Gson(),
) {
    suspend fun uploadSelectedMedia(
        context: Context,
        projectId: String,
        uris: List<Uri>,
    ): List<MaterialItem> {
        return withContext(Dispatchers.IO) {
            val boundary = "Boundary-${UUID.randomUUID()}"
            val url = URL(YrsApiConfig.api("/uploads/files"))
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 60_000
                doOutput = true
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                setRequestProperty("Accept", "application/json")
            }

            try {
                DataOutputStream(connection.outputStream).use { output ->
                    writeTextPart(output, boundary, "projectId", projectId)
                    uris.forEachIndexed { index, uri ->
                        writeFilePart(
                            output = output,
                            boundary = boundary,
                            name = "files",
                            context = context,
                            uri = uri,
                            fallbackName = "upload_$index",
                        )
                    }
                    output.writeBytes("--$boundary--\r\n")
                }

                val responseCode = connection.responseCode
                val body = (if (responseCode in 200..299) connection.inputStream else connection.errorStream)
                    ?.bufferedReader(StandardCharsets.UTF_8)
                    ?.readText()
                    .orEmpty()
                if (responseCode !in 200..299) {
                    throw IllegalStateException("HTTP $responseCode: $body")
                }

                val responseType = object : TypeToken<ApiEnvelope<List<MaterialPayload>>>() {}.type
                val payload = gson.fromJson<ApiEnvelope<List<MaterialPayload>>>(body, responseType)
                payload.data.orEmpty().map {
                    MaterialItem(
                        materialId = it.materialId,
                        title = it.title,
                        type = when (it.materialType) {
                            "VIDEO" -> MaterialType.VIDEO
                            else -> MaterialType.PHOTO
                        },
                        durationLabel = it.durationLabel,
                        insight = it.insight,
                    )
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun writeTextPart(
        output: DataOutputStream,
        boundary: String,
        name: String,
        value: String,
    ) {
        output.writeBytes("--$boundary\r\n")
        output.writeBytes("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
        output.writeBytes(value)
        output.writeBytes("\r\n")
    }

    private fun writeFilePart(
        output: DataOutputStream,
        boundary: String,
        name: String,
        context: Context,
        uri: Uri,
        fallbackName: String,
    ) {
        val contentResolver = context.contentResolver
        val fileName = queryDisplayName(contentResolver, uri) ?: fallbackName
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        val bytes = contentResolver.openInputStream(uri)?.use { input ->
            BufferedInputStream(input).readBytes()
        } ?: ByteArray(0)

        output.writeBytes("--$boundary\r\n")
        output.writeBytes("Content-Disposition: form-data; name=\"$name\"; filename=\"$fileName\"\r\n")
        output.writeBytes("Content-Type: $mimeType\r\n\r\n")
        output.write(bytes)
        output.writeBytes("\r\n")
    }

    private fun queryDisplayName(contentResolver: ContentResolver, uri: Uri): String? {
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    return it.getString(index)
                }
            }
        }
        return null
    }
}

private data class ApiEnvelope<T>(
    val requestId: String,
    val data: T?,
    val error: Map<String, Any?>? = null,
)

private data class MaterialPayload(
    val materialId: String,
    val title: String,
    val materialType: String,
    val durationLabel: String,
    val insight: String,
)
