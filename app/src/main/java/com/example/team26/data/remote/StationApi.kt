package com.example.team26.data.remote

import com.example.team26.domain.station.ApiStationEntity
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface StationApiService {
    @GET("devices")
    suspend fun getAllDevices(): List<ApiStationEntity>

    @GET("devices/{identifier}")
    suspend fun getDeviceByIdentifier(@Path("identifier") identifier: String): ApiStationEntity

    @POST("devices/{identifier}/state")
    suspend fun setDeviceState(
        @Path("identifier") identifier: String,
        @Body state: Map<String, String>
    ): ApiStationEntity

    @POST("devices/{identifier}/astroman")
    suspend fun setDeviceMode(
        @Path("identifier") identifier: String,
        @Body mode: Map<String, String>
    ): ApiStationEntity
}
