package com.jammking.webprobe.common.http

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class OkHttpClientWrapper(
    private val client: OkHttpClient = defaultClient()
): HttpClient {

    override fun get(
        url: String,
        headers: Map<String, String>
    ): HttpResponse {
        val request = Request.Builder().url(url).apply {
            headers.forEach { (k, v) -> addHeader(k, v)}
        }.build()

        return client.newCall(request).execute().use { response ->
            HttpResponse(
                statusCode = response.code,
                body = response.body?.string(),
                headers = response.headers.toMultimap()
            )
        }
    }

    companion object {
        fun defaultClient() = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .build()
    }
}