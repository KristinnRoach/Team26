package com.example.team26.data.remote

import com.example.team26.data.station.StationDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface StationApi {
    @GET("stations")
    suspend fun getAllStations(): List<StationDto>

    @GET("stations/{stationId}")
    suspend fun getStationById(@Path("stationId") stationId: String): StationDto

    @POST("devices/{id}/state")
    suspend fun updateStationState(
        @Path("id") id: String,
        @Body state: String
    ): StationDto

    @POST("devices/{id}/astroman")
    suspend fun updateStationMode(
        @Path("id") id: String,
        @Body mode: String
    ): StationDto
}


