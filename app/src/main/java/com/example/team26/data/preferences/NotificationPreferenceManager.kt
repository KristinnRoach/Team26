package com.example.team26.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.team26.domain.notification.NotificationPreference

class NotificationPreferenceManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "notification_prefs"
        private fun getKey(preference: NotificationPreference) = "pref_${preference.name}"
    }

    fun isPreferenceEnabled(preference: NotificationPreference): Boolean {
        return prefs.getBoolean(getKey(preference), preference.defaultEnabled)
    }

    fun setPreferenceEnabled(preference: NotificationPreference, isEnabled: Boolean) {
        prefs.edit().putBoolean(getKey(preference), isEnabled).apply()
    }

    // Get all preferences
    fun getAllPreferences(): Map<NotificationPreference, Boolean> {
        return NotificationPreference.values().associateWith { isPreferenceEnabled(it) }
    }
}