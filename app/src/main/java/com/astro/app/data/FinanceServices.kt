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

@Serializable data class FxResponse(val base:String="EUR", val rates:Map<String,Double> = emptyMap())
class FxService { private val client=OkHttpClient(); private val json=Json{ignoreUnknownKeys=true}; suspend fun latest(base:String="EUR")=withContext(Dispatchers.IO){ val req=Request.Builder().url("https://api.frankfurter.app/latest?from=$base").build(); client.newCall(req).execute().use{r->if(!r.isSuccessful) error("FX HTTP ${r.code}"); json.decodeFromString<FxResponse>(r.body!!.string())} } }
class AiService(private val context:Context){ private val client=OkHttpClient(); suspend fun ask(question:String):Result<String> = runCatching { val url=com.astro.app.BuildConfig.AI_BASE_URL.trimEnd('/'); require(url.isNotBlank()){"AI_BASE_URL is not configured"}; val body="{\"message\":${Json.encodeToString(kotlinx.serialization.serializer<String>(),question)}}"; val token=context.getSharedPreferences("astro_auth",Context.MODE_PRIVATE).getString("access",null); val b=Request.Builder().url(url).post(body.toRequestBody("application/json".toMediaType())); if(!token.isNullOrBlank()) b.addHeader("Authorization","Bearer $token"); client.newCall(b.build()).execute().use{r->val raw=r.body!!.string(); if(!r.isSuccessful) error("AI HTTP ${r.code}: $raw"); runCatching{Json.decodeFromString<String>(raw)}.getOrElse{raw}} } }
