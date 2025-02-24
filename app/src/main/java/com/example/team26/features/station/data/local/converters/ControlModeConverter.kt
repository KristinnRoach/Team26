package com.example.team26.features.station.data.local.converters

import androidx.room.TypeConverter
import com.example.team26.features.station.domain.model.ControlMode

class ControlModeConverter {
    @TypeConverter
    fun fromControlMode(mode: ControlMode): String = mode.name

    @TypeConverter
    fun toControlMode(modeName: String): ControlMode =
        ControlMode.valueOf(modeName)
}