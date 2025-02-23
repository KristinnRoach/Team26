package com.example.team26.di

import android.content.Context
// import androidx.privacysandbox.tools.core.generator.build
import androidx.room.Room
import com.example.team26.data.local.PilotAppDB
import com.example.team26.data.local.dao.StationDao
import com.example.team26.data.remote.api.StationApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideStationApi(): StationApi =
        Retrofit.Builder()
            .baseUrl("https://your-api-base-url/")
            // .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StationApi::class.java)

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): PilotAppDB {
        return Room.databaseBuilder(
            context,
            PilotAppDB::class.java,
            "pilot_app_db" // Name of your database
        ).build()
    }

    @Provides
    fun provideStationDao(pilotAppDB: PilotAppDB): StationDao {
        return pilotAppDB.stationDao()
    }
    // Other provider methods...
}