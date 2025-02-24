package com.example.team26.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
// import androidx.room.TypeConverters
import com.example.team26.features.station.data.local.dao.StationDao
import com.example.team26.features.station.data.local.entity.StationEntity
// import com.example.team26.features.station.data.local.converters.StationStateConverter
// import com.example.team26.features.station.data.local.converters.ControlModeConverter

@Database(
    entities = [StationEntity::class],
    version = 1
)
abstract class PilotAppDB : RoomDatabase() {
    abstract fun stationDao(): StationDao
}