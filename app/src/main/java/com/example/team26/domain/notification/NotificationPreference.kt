package com.example.team26.domain.notification

enum class NotificationPreference(val defaultEnabled: Boolean = true) {
    // Critical Issues
    STATION_ERROR(true),        // Station enters ERROR state
    API_SYNC_FAILURE(true),     // API sync fails

    // Operational Changes
    STATE_CHANGE(false),        // Normal ON/OFF change
    MODE_CHANGE(false),         // Normal mode change (ASTRO/MANUAL)

    // Connectivity/Presence
    STATION_ADDED_REMOVED(true), // Station added or removed

    // Recovery
    STATION_RECOVERY(true)      // Station recovers from error
}