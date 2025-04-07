package com.example.team26.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.team26.data.AppDb
import com.example.team26.data.preferences.ApiSettingsManager
import com.example.team26.data.preferences.NotificationPreferenceManager
import com.example.team26.data.remote.StationApiService
import com.example.team26.data.station.ApiStationRepositoryImpl
import com.example.team26.domain.notification.NotificationDao
import com.example.team26.domain.station.StationDao
import com.example.team26.domain.station.StationRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object AppModule {

    private var dbInstance: AppDb? = null
    private var preferenceManagerInstance: NotificationPreferenceManager? = null
    private var apiSettingsManagerInstance: ApiSettingsManager? = null

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
        if (apiSettingsManagerInstance == null) {
            apiSettingsManagerInstance = ApiSettingsManager(context.applicationContext)
        }

    }

    private val database: AppDb
        get() = dbInstance ?: throw IllegalStateException("AppModule not initialized.")

    val notificationPreferenceManager: NotificationPreferenceManager
        get() = preferenceManagerInstance ?: throw IllegalStateException("AppModule not initialized.")

    val apiSettingsManager: ApiSettingsManager
        get() = apiSettingsManagerInstance ?: throw IllegalStateException("AppModule not initialized.")

    /*
    This approach means you need to restart the app whenever API/username/pass are changed
     */
    private val okHttpClient: OkHttpClient by lazy {
        val currentApiSettings = apiSettingsManager
        val clientId = currentApiSettings.getClientId()
        val clientSecret = currentApiSettings.getClientSecret()

        val builder = OkHttpClient.Builder()

        builder.addInterceptor(object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()

                // Add headers
                if (clientId.isNotEmpty()) {
                    Log.d("AppModule", "Adding CF-Access-Client-Id header")
                    requestBuilder.header("CF-Access-Client-Id", clientId)
                } else {
                    Log.d("AppModule", "Client ID is empty, not adding header.")
                }
                if (clientSecret.isNotEmpty()) {
                    Log.d("AppModule", "Adding CF-Access-Client-Secret header")
                    requestBuilder.header("CF-Access-Client-Secret", clientSecret)
                } else {
                    Log.d("AppModule", "Client Secret is empty, not adding header.")
                }

                requestBuilder.method(originalRequest.method, originalRequest.body)

                val newRequest = requestBuilder.build()
                return chain.proceed(newRequest)
            }
        })
        builder.build()
    }

    private val retrofit: Retrofit by lazy {
        val currentApiSettings = apiSettingsManager
        Retrofit.Builder()
            .baseUrl(currentApiSettings.getApiUrl())
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