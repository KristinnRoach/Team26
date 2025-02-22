package com.example.team26.data.remote.api

interface StationApi {
    @GET("stations")
    suspend fun getStations(): Response<List<StationDto>>

    @POST("devices/{id}/state")
    suspend fun updateStationState(
        @Path("id") id: String,
        @Body state: StateUpdateRequest
    ): Response<StationDto>

    @POST("devices/{id}/astroman")
    suspend fun updateControlMode(
        @Path("id") id: String,
        @Body mode: ControlModeUpdateRequest
    ): Response<StationDto>
}