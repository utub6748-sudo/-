package com.astro.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable data class AuthSession(val access_token: String? = null, val refresh_token: String? = null, val user: User? = null)
@Serializable data class User(val id: String? = null, val email: String? = null)

class AuthRepository(private val context: Context) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val prefs = context.getSharedPreferences("astro_auth", Context.MODE_PRIVATE)
    private val base get() = com.astro.app.BuildConfig.SUPABASE_URL.trimEnd('/')
    private val key get() = com.astro.app.BuildConfig.SUPABASE_KEY

    private fun request(path: String, body: String): String {
        require(base.isNotBlank() && key.isNotBlank()) { "Supabase is not configured. Add SUPABASE_URL and SUPABASE_KEY in app build config." }
        val req = Request.Builder().url("$base$path").addHeader("apikey", key).addHeader("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType())).build()
        client.newCall(req).execute().use { r -> if (!r.isSuccessful) error(r.body?.string() ?: "HTTP ${r.code}"); return r.body!!.string() }
    }

    suspend fun signUp(email: String, password: String): Result<String> = runCatching {
        withContext(Dispatchers.IO) { request("/auth/v1/signup", "{\"email\":${quote(email)},\"password\":${quote(password)}}") }
        "Код подтверждения отправлен на $email"
    }

    suspend fun verifyEmail(email: String, token: String): Result<String> = runCatching {
        val response = withContext(Dispatchers.IO) { request("/auth/v1/verify", "{\"type\":\"signup\",\"email\":${quote(email)},\"token\":${quote(token)}}") }
        val session = json.decodeFromString<AuthSession>(response)
        save(session)
        "Email подтвержден"
    }

    suspend fun login(email: String, password: String): Result<String> = runCatching {
        val response = withContext(Dispatchers.IO) { request("/auth/v1/token?grant_type=password", "{\"email\":${quote(email)},\"password\":${quote(password)}}") }
        val session = json.decodeFromString<AuthSession>(response); save(session); "Вход выполнен"
    }

    fun logout() { prefs.edit().clear().apply() }
    fun isLoggedIn(): Boolean = !prefs.getString("access", null).isNullOrBlank()
    fun email(): String? = prefs.getString("email", null)

    private fun save(s: AuthSession) { prefs.edit().putString("access", s.access_token).putString("refresh", s.refresh_token).putString("email", s.user?.email).apply() }
    private fun quote(s: String) = Json.encodeToString(kotlinx.serialization.serializer<String>(), s)
}
