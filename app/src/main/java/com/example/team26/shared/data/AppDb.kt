package com.example.team26.shared.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.team26.shared.domain.station.StationDao
import com.example.team26.shared.domain.station.StationEntity
// import androidx.room.TypeConverters

@Database(
    entities = [StationEntity::class],
    version = 1
)
abstract class AppDb : RoomDatabase() {
    abstract fun stationDao(): StationDao
}