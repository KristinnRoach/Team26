package com.example.team26.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.team26.data.converters.LocationConverters
import com.example.team26.domain.notification.LastKnownStationStateEntity // Added
import com.example.team26.domain.notification.NotificationDao // Added
import com.example.team26.domain.notification.NotificationEntity // Added
import com.example.team26.domain.station.StationDao
import com.example.team26.domain.station.StationEntity

@Database(
    entities = [
        StationEntity::class,
        NotificationEntity::class,
        LastKnownStationStateEntity::class
    ],
    version = 1
)
@TypeConverters(LocationConverters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun stationDao(): StationDao
    abstract fun notificationDao(): NotificationDao
}