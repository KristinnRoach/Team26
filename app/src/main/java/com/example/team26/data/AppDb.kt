package com.example.team26.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.team26.domain.station.StationDao
import com.example.team26.domain.station.StationEntity
import androidx.room.TypeConverters
import com.example.team26.data.converters.LocationConverters

@Database(
    entities = [StationEntity::class],
    version = 1
)
@TypeConverters(LocationConverters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun stationDao(): StationDao
}