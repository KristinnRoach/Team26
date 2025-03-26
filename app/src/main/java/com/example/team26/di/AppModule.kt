package com.example.team26.di

import com.example.team26.data.station.ApiStationRepositoryImpl
import com.example.team26.data.station.MockStationRepositoryImpl
import com.example.team26.domain.station.StationRepository



// Simplified version without DI
object AppModule {
    val stationRepository: StationRepository = ApiStationRepositoryImpl()
}

/*
// Over complicated version with DI - ignore for now

import android.content.Context
import com.example.team26.domain.auth.AuthRepository
import com.example.team26.data.auth.AuthApi
import com.example.team26.data.auth.AuthRepositoryImpl
import com.example.team26.data.station.MockStationRepositoryImpl
import com.example.team26.domain.station.StationRepository
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.converter.gson.GsonConverterFactory


interface AppModule {
    // val stationAPI: StationApi // later
    val stationRepository: StationRepository
    val authApi: AuthApi
    val authRepository: AuthRepository
}

class AppModuleImpl(
    private val appContext: Context
): AppModule {

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
*/


