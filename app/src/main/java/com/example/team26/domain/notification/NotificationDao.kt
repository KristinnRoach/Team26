package com.example.team26.domain.notification

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(id) FROM notifications WHERE isRead = 0")
    fun getUnreadNotificationCount(): Flow<Int>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markNotificationAsRead(notificationId: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    @Query("SELECT * FROM last_known_station_states")
    suspend fun getAllLastKnownStates(): List<LastKnownStationStateEntity>

    // Update all last known states
    @Transaction
    suspend fun updateAllLastKnownStates(newStates: List<LastKnownStationStateEntity>) {
        clearLastKnownStates()
        insertLastKnownStates(newStates)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLastKnownStates(states: List<LastKnownStationStateEntity>)

    @Query("DELETE FROM last_known_station_states")
    suspend fun clearLastKnownStates()
}