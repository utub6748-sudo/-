package com.astro.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import android.content.Context
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable data class FxResponse(val base: String = "EUR", val rates: Map<String, Double> = emptyMap())

class FxService {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    suspend fun latest(base: String = "EUR"): FxResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder().url("https://api.frankfurter.app/latest?from=$base").build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("FX HTTP ${response.code}")
            json.decodeFromString(response.body!!.string())
        }
    }
}

class AiService(private val context: Context) {
    private val client = OkHttpClient()
    suspend fun ask(question: String): Result<String> = runCatching {
        val url = com.astro.app.BuildConfig.AI_BASE_URL.trimEnd('/')
        require(url.isNotBlank()) { "AI_BASE_URL is not configured. Deploy the included Supabase Edge Function and set its URL." }
        val body = "{\"message\":${Json.encodeToString(kotlinx.serialization.serializer<String>(), question)}}"
        val token = context.getSharedPreferences("astro_auth", Context.MODE_PRIVATE).getString("access", null)
        val requestBuilder = Request.Builder().url(url).post(body.toRequestBody("application/json".toMediaType()))
        if (!token.isNullOrBlank()) requestBuilder.addHeader("Authorization", "Bearer $token")
        val request = requestBuilder.build()
        client.newCall(request).execute().use { r -> val raw = r.body!!.string(); if (!r.isSuccessful) error("AI HTTP ${r.code}: $raw"); runCatching { Json.decodeFromString<String>(raw) }.getOrElse { raw } }
    }
}
