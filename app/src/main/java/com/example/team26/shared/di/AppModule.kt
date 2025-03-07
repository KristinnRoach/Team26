package com.example.team26.shared.di

import android.content.Context
import com.example.team26.shared.data.remote.MockStationApi
import com.example.team26.shared.data.remote.StationApi
import com.example.team26.shared.domain.auth.AuthRepository
import com.example.team26.shared.data.auth.AuthApi
import com.example.team26.shared.data.auth.AuthRepositoryImpl
import com.example.team26.shared.data.station.MockStationRepositoryImpl
import com.example.team26.shared.domain.station.StationRepository
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.converter.gson.GsonConverterFactory

interface AppModule {
    val stationAPI: StationApi
    val stationRepository: StationRepository
    val authApi: AuthApi
    val authRepository: AuthRepository
}

class AppModuleImpl(
    private val appContext: Context
): AppModule {
    override val stationAPI: StationApi by lazy {
        MockStationApi()
    }

    override val stationRepository: StationRepository by lazy {
        MockStationRepositoryImpl()
    }

    override val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://my-url.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create()
    }
    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApi)
    }
}


/*
    override val stationAPI: StationApi by lazy {
Retrofit.Builder()
    .baseUrl("https://my-url.com")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create()
}
 */

/*
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Only app-wide general dependencies
}
*/
