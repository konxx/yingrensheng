package com.yingrensheng.core.network

import com.google.gson.annotations.SerializedName

data class NetworkError(
    val code: String,
    val message: String,
    val details: Map<String, Any?> = emptyMap(),
)

data class NetworkApiResponse<T>(
    @SerializedName("requestId")
    val requestId: String,
    val data: T? = null,
    val error: NetworkError? = null,
)

data class NetworkPage<T>(
    val items: List<T>,
    val page: Int,
    @SerializedName("pageSize")
    val pageSize: Int,
    @SerializedName("hasMore")
    val hasMore: Boolean,
    @SerializedName("nextCursor")
    val nextCursor: String? = null,
)

fun <T> NetworkApiResponse<T>.requireData(): T {
    return data ?: error?.let { throw IllegalStateException("${it.code}: ${it.message}") }
    ?: throw IllegalStateException("Missing response data for request $requestId")
}

