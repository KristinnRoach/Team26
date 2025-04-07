package com.example.team26.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.IOException
import java.security.GeneralSecurityException

class ApiSettingsManager(context: Context) {

    companion object {
        private const val TAG = "ApiSettingsManager"
        private const val API_PREFS_NAME = "api_settings_prefs"
        private const val ENCRYPTED_API_PREFS_NAME = "encrypted_api_settings_prefs"
        private const val KEY_API_URL = "api_url"
        private const val KEY_CLIENT_ID = "api_client_id"
        private const val KEY_CLIENT_SECRET = "api_client_secret"
        private const val KEY_LAST_KNOWN_API_REACHABLE = "last_known_api_reachable"
        const val DEFAULT_API_URL = "http://10.0.2.2:5053/"
    }

    private val appContext = context.applicationContext

    private val prefs: SharedPreferences = appContext.getSharedPreferences(
        API_PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val encryptedPrefs: SharedPreferences? = try {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            ENCRYPTED_API_PREFS_NAME,
            masterKeyAlias,
            appContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: GeneralSecurityException) {
        Log.e(TAG, "EncryptedPrefs init error", e)
        null
    } catch (e: IOException) {
        Log.e(TAG, "EncryptedPrefs init error", e)
        null
    }

    init {
        Log.d(TAG, "ApiSettingsManager Initialized. Current Reachable State on init: ${getLastKnownApiReachableState()}")
    }


    fun getApiUrl(): String {
        return prefs.getString(KEY_API_URL, DEFAULT_API_URL) ?: DEFAULT_API_URL
    }

    fun setApiUrl(url: String) {
        prefs.edit().putString(KEY_API_URL, url).apply()
    }

    fun getClientId(): String {
        return encryptedPrefs?.getString(KEY_CLIENT_ID, "") ?: ""
    }

    fun setClientId(clientId: String) {
        encryptedPrefs?.edit()?.putString(KEY_CLIENT_ID, clientId)?.apply()
            ?: Log.w(TAG, "Cannot save Client ID")
    }

    fun getClientSecret(): String {
        return encryptedPrefs?.getString(KEY_CLIENT_SECRET, "") ?: ""
    }

    fun setClientSecret(clientSecret: String) {
        encryptedPrefs?.edit()?.putString(KEY_CLIENT_SECRET, clientSecret)?.apply()
            ?: Log.w(TAG, "Cannot save Client Secret")
    }

    fun getLastKnownApiReachableState(): Boolean {
        val isReachable = prefs.getBoolean(KEY_LAST_KNOWN_API_REACHABLE, true)
        Log.d(TAG, "Getting Last Known API Reachable State. Returning: $isReachable")
        return isReachable
    }

    fun setLastKnownApiReachableState(isReachable: Boolean) {
        Log.d(TAG, "Setting Last Known API Reachable State to: $isReachable")
        prefs.edit().putBoolean(KEY_LAST_KNOWN_API_REACHABLE, isReachable).apply()
        Log.d(TAG, "Attempted to save Last Known API Reachable State: $isReachable")
    }
}