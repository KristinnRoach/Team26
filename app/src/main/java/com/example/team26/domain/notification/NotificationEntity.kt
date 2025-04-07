package com.example.team26.domain.notification

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val message: String,
    val type: NotificationType,
    val stationId: String?,
    val isRead: Boolean = false
)

enum class NotificationType {
    STATE_CHANGE, MODE_CHANGE, STATION_ADDED, STATION_REMOVED, SYNC_ERROR, INFO
}