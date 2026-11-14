package com.example.trdial.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.trDialDataStore: DataStore<Preferences> by preferencesDataStore(name = "tr_dial_settings")

data class UserSettings(
    val languageCode: String = "en",
    val onboardingCompleted: Boolean = false,
    val loggedInUserId: Long? = null,
    val preferredCallingMode: String = "SIP_TO_SIP",
    val echoCancellation: Boolean = true,
    val noiseSuppression: Boolean = true,
    val autoProximitySpeaker: Boolean = true,
    val wifiOnlyCalls: Boolean = false
)

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LOGGED_IN_USER_ID = longPreferencesKey("logged_in_user_id")
        val PREFERRED_CALLING_MODE = stringPreferencesKey("preferred_calling_mode")
        val ECHO_CANCELLATION = booleanPreferencesKey("echo_cancellation")
        val NOISE_SUPPRESSION = booleanPreferencesKey("noise_suppression")
        val AUTO_PROXIMITY_SPEAKER = booleanPreferencesKey("auto_proximity_speaker")
        val WIFI_ONLY_CALLS = booleanPreferencesKey("wifi_only_calls")
    }

    val settingsFlow: Flow<UserSettings> = context.trDialDataStore.data.map { prefs ->
        UserSettings(
            languageCode = prefs[Keys.LANGUAGE_CODE] ?: "en",
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            loggedInUserId = prefs[Keys.LOGGED_IN_USER_ID],
            preferredCallingMode = prefs[Keys.PREFERRED_CALLING_MODE] ?: "SIP_TO_SIP",
            echoCancellation = prefs[Keys.ECHO_CANCELLATION] ?: true,
            noiseSuppression = prefs[Keys.NOISE_SUPPRESSION] ?: true,
            autoProximitySpeaker = prefs[Keys.AUTO_PROXIMITY_SPEAKER] ?: true,
            wifiOnlyCalls = prefs[Keys.WIFI_ONLY_CALLS] ?: false
        )
    }

    suspend fun setLanguageCode(code: String) {
        context.trDialDataStore.edit { it[Keys.LANGUAGE_CODE] = code }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.trDialDataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setLoggedInUserId(userId: Long?) {
        context.trDialDataStore.edit { prefs ->
            if (userId == null) prefs.remove(Keys.LOGGED_IN_USER_ID)
            else prefs[Keys.LOGGED_IN_USER_ID] = userId
        }
    }

    suspend fun setPreferredCallingMode(mode: String) {
        context.trDialDataStore.edit { it[Keys.PREFERRED_CALLING_MODE] = mode }
    }

    suspend fun setEchoCancellation(enabled: Boolean) {
        context.trDialDataStore.edit { it[Keys.ECHO_CANCELLATION] = enabled }
    }

    suspend fun setNoiseSuppression(enabled: Boolean) {
        context.trDialDataStore.edit { it[Keys.NOISE_SUPPRESSION] = enabled }
    }

    suspend fun setAutoProximitySpeaker(enabled: Boolean) {
        context.trDialDataStore.edit { it[Keys.AUTO_PROXIMITY_SPEAKER] = enabled }
    }

    suspend fun setWifiOnlyCalls(enabled: Boolean) {
        context.trDialDataStore.edit { it[Keys.WIFI_ONLY_CALLS] = enabled }
    }
}
