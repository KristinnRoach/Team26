package com.example.team26.di

import android.content.Context
import androidx.room.Room
import com.example.team26.data.AppDb
import com.example.team26.data.preferences.NotificationPreferenceManager
import com.example.team26.data.remote.StationApiService
import com.example.team26.data.station.ApiStationRepositoryImpl
import com.example.team26.domain.notification.NotificationDao
import com.example.team26.domain.station.StationDao
import com.example.team26.domain.station.StationRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AppModule {

    private var dbInstance: AppDb? = null
    private var preferenceManagerInstance: NotificationPreferenceManager? = null

    fun initialize(context: Context) {
        if (dbInstance == null) {
            dbInstance = Room.databaseBuilder(
                context.applicationContext,
                AppDb::class.java, "pilot-app-db"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
        if (preferenceManagerInstance == null) {
            preferenceManagerInstance = NotificationPreferenceManager(context.applicationContext)
        }
    }

    private val database: AppDb
        get() = dbInstance ?: throw IllegalStateException("AppModule not initialized.")

    val notificationPreferenceManager: NotificationPreferenceManager
        get() = preferenceManagerInstance ?: throw IllegalStateException("AppModule not initialized.")

    private const val BASE_URL = "http://10.0.2.2:5053/"
    private const val CLIENT_ID = ""
    private const val CLIENT_SECRET = ""

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("CF-Access-Client-Id", CLIENT_ID)
                    .addHeader("CF-Access-Client-Secret", CLIENT_SECRET)
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val stationApiService: StationApiService by lazy {
        retrofit.create(StationApiService::class.java)
    }

    // Repository
    val stationRepository: StationRepository by lazy {
        ApiStationRepositoryImpl(stationApiService)
    }

    val notificationDao: NotificationDao by lazy {
        database.notificationDao()
    }

    val stationDao: StationDao by lazy {
        database.stationDao()
    }
}