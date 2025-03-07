package com.example.team26.shared.di




import android.content.Context
import androidx.room.Room
import com.example.team26.shared.data.AppDb
import com.example.team26.shared.domain.station.StationDao

class DatabaseModule(private val context: Context) {
    private val appDb: AppDb by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDb::class.java,
            "pilot_app_db"
        ).build()
    }

    val stationDao: StationDao by lazy {
        appDb.stationDao()
    }
}