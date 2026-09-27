package com.astro.app.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.settingsDataStore by preferencesDataStore("astro_settings")

class AstroRepository(context: Context) {
    private val db = AstroDatabase.get(context)
    val dao = db.dao()
    private val store = context.settingsDataStore
    val theme: Flow<String> = store.data.map { it[Keys.THEME] ?: "system" }
    val accent: Flow<Long> = store.data.map { it[Keys.ACCENT] ?: 0xFF8B5CF6 }
    val nickname: Flow<String> = store.data.map { it[Keys.NICKNAME] ?: "Astro User" }
    val baseCurrency: Flow<String> = store.data.map { it[Keys.BASE_CURRENCY] ?: "EUR" }
    val avatarUri: Flow<String> = store.data.map { it[Keys.AVATAR] ?: "" }
    val pinEnabled: Flow<Boolean> = store.data.map { it[Keys.PIN_HASH] != null }
    suspend fun setTheme(value: String) = store.edit { it[Keys.THEME] = value }
    suspend fun setAccent(value: Long) = store.edit { it[Keys.ACCENT] = value }
    suspend fun setNickname(value: String) = store.edit { it[Keys.NICKNAME] = value }
    suspend fun setBaseCurrency(value: String) = store.edit { it[Keys.BASE_CURRENCY] = value }
    suspend fun setAvatar(value: String) = store.edit { it[Keys.AVATAR] = value }
    suspend fun setPin(pin: String) = store.edit { it[Keys.PIN_HASH] = sha256(pin) }
    suspend fun verifyPin(pin: String): Boolean = store.data.map { it[Keys.PIN_HASH] == sha256(pin) }.let { kotlinx.coroutines.flow.first(it) }
    suspend fun clearPin() = store.edit { it.remove(Keys.PIN_HASH) }
    suspend fun seed() { dao.insertCategories(listOf(CategoryEntity(name="Продукты",icon="⌂"),CategoryEntity(name="Транспорт",icon="↗"),CategoryEntity(name="Дом",icon="□"),CategoryEntity(name="Здоровье",icon="+"),CategoryEntity(name="Развлечения",icon="○"),CategoryEntity(name="Подписки",icon="∞"),CategoryEntity(name="Другое",icon="⋯"))) }
    private object Keys { val THEME=stringPreferencesKey("theme"); val ACCENT=longPreferencesKey("accent"); val NICKNAME=stringPreferencesKey("nickname"); val BASE_CURRENCY=stringPreferencesKey("base_currency"); val PIN_HASH=stringPreferencesKey("pin_hash"); val AVATAR=stringPreferencesKey("avatar") }
    private fun sha256(s: String): String = MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
}
