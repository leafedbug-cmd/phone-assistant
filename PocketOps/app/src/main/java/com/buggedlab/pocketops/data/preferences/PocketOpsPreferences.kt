package com.buggedlab.pocketops.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pocketops_settings")

class PocketOpsPreferences(private val context: Context) {

    private val encryptedPrefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "pocketops_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to standard private preferences if keystore is unavailable in some environments
            context.getSharedPreferences("pocketops_secure_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    companion object {
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_AI_OPT_IN = booleanPreferencesKey("ai_opt_in")
        private val KEY_ACTIVE_MODE = stringPreferencesKey("active_mode")
        private val KEY_GEMINI_MODEL = stringPreferencesKey("gemini_model")
        private val KEY_DIGEST_TIME = stringPreferencesKey("digest_time")
        private val KEY_DIGEST_ENABLED = booleanPreferencesKey("digest_enabled")
        private val KEY_EMERGENCY_STOP = booleanPreferencesKey("emergency_stop")

        private const val SECURE_API_KEY = "gemini_api_key"
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    val isAiOptedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AI_OPT_IN] ?: false
    }

    suspend fun setAiOptIn(optedIn: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AI_OPT_IN] = optedIn
        }
    }

    val activeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_MODE] ?: "Standard"
    }

    suspend fun setActiveMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACTIVE_MODE] = mode
        }
    }

    val geminiModel: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_GEMINI_MODEL] ?: "gemini-2.0-flash"
    }

    suspend fun setGeminiModel(model: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GEMINI_MODEL] = model
        }
    }

    val isEmergencyStopActive: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_EMERGENCY_STOP] ?: false
    }

    suspend fun setEmergencyStopActive(active: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EMERGENCY_STOP] = active
        }
    }

    val digestTime: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DIGEST_TIME] ?: "18:00"
    }

    val isDigestEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DIGEST_ENABLED] ?: true
    }

    suspend fun setDigestConfig(enabled: Boolean, time: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DIGEST_ENABLED] = enabled
            prefs[KEY_DIGEST_TIME] = time
        }
    }

    // Keystore-backed API Key Storage
    fun getApiKey(): String? {
        return encryptedPrefs.getString(SECURE_API_KEY, null)?.takeIf { it.isNotBlank() }
    }

    fun setApiKey(apiKey: String?) {
        encryptedPrefs.edit().apply {
            if (apiKey.isNullOrBlank()) {
                remove(SECURE_API_KEY)
            } else {
                putString(SECURE_API_KEY, apiKey.trim())
            }
            apply()
        }
    }

    fun hasApiKey(): Boolean = !getApiKey().isNullOrBlank()
}
