package com.example.team26.data.converters

import androidx.room.TypeConverter
import com.example.team26.domain.station.Location
import com.google.gson.Gson

class LocationConverters {
    private val gson = Gson()

    @TypeConverter
    fun locationToString(location: Location): String {
        return gson.toJson(location)
    }

    @TypeConverter
    fun stringToLocation(value: String): Location {
        return gson.fromJson(value, Location::class.java)
    }
}