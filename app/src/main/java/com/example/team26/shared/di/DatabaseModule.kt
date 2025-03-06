package com.example.team26.shared.di

import android.content.Context
import androidx.room.Room
import com.example.team26.shared.data.local.PilotAppDB
import com.example.team26.features.station.data.local.dao.StationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton  // todo: look into this (i.e. versus jakarta or something from hilt)

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): PilotAppDB {
        return Room.databaseBuilder(
            context,
            PilotAppDB::class.java,
            "pilot_app_db"
        ).build()
    }

    @Provides
    fun provideStationDao(pilotAppDB: PilotAppDB): StationDao {
        return pilotAppDB.stationDao()
    }
}