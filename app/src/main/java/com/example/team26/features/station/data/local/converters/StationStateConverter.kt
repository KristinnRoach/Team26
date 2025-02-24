package com.example.team26.features.station.data.local.converters

import androidx.room.TypeConverter
import com.example.team26.features.station.domain.model.StationState

class StationStateConverter {
    @TypeConverter
    fun fromStationState(state: StationState): String = state.name

    @TypeConverter
    fun toStationState(stateName: String): StationState =
        StationState.valueOf(stateName)
}