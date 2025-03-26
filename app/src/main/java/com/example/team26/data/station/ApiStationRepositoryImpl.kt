package com.example.team26.data.station

import android.util.Log
import com.example.team26.data.converters.StationConverter
import com.example.team26.domain.station.ApiStationEntity
import com.example.team26.domain.station.ControlMode
import com.example.team26.domain.station.StationEntity
import com.example.team26.domain.station.StationRepository
import com.example.team26.domain.station.DeviceState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

private const val BASE_URL = "http://10.0.2.2:5053/"
private const val CLIENT_ID = ""
private const val CLIENT_SECRET = ""

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

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .client(
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("CF-Access-Client-Id", CLIENT_ID)
                    .addHeader("CF-Access-Client-Secret", CLIENT_SECRET)
                    .build()
                chain.proceed(request)
            }
            .build()
    )
    .build()

private val apiService: StationApiService = retrofit.create(StationApiService::class.java)

class ApiStationRepositoryImpl : StationRepository {

    override suspend fun refreshStations() {
        Log.d("STATION_DEBUG", "Repository: refreshStations called (no-op)")
    }

    override fun getAllStations(): Flow<List<StationEntity>> = flow {
        try {
            val apiStations = apiService.getAllDevices()
            val stations = apiStations.map { StationConverter.toStationEntity(it) }
            Log.d("STATION_DEBUG", "Repository: getAllStations fetched ${stations.size} stations")
            emit(stations)
        } catch (e: Exception) {
            Log.e("STATION_DEBUG", "Repository: getAllStations failed", e)
            emit(emptyList())
        }
    }

    override suspend fun getStationById(id: String): Flow<StationEntity?> {
        return flowOf(
            try {
                val apiStation = apiService.getDeviceByIdentifier(id)
                val station = StationConverter.toStationEntity(apiStation)
                Log.d("STATION_DEBUG", "Repository: getStationById fetched id $id: ${station.name}")
                station
            } catch (e: Exception) {
                Log.e("STATION_DEBUG", "Repository: getStationById failed for id $id", e)
                null
            }
        )
    }

    override suspend fun updateStationState(id: String, state: DeviceState): Result<StationEntity> {
        return try {
            val stateMap = mapOf("state" to state.name)
            val updatedApiStation = apiService.setDeviceState(id, stateMap)
            val updatedStation = StationConverter.toStationEntity(updatedApiStation)
            Log.d("STATION_DEBUG", "Repository: updateStationState updated id $id to state $state")
            Result.success(updatedStation)
        } catch (e: Exception) {
            Log.e("STATION_DEBUG", "Repository: updateStationState failed for id $id", e)
            Result.failure(e)
        }
    }

    override suspend fun updateControlMode(id: String, mode: ControlMode): Result<StationEntity> {
        return try {
            val modeMap = mapOf("astroman" to mode.name)
            val updatedApiStation = apiService.setDeviceMode(id, modeMap)
            val updatedStation = StationConverter.toStationEntity(updatedApiStation)
            Log.d("STATION_DEBUG", "Repository: updateControlMode updated id $id to mode $mode")
            Result.success(updatedStation)
        } catch (e: Exception) {
            Log.e("STATION_DEBUG", "Repository: updateControlMode failed for id $id", e)
            Result.failure(e)
        }
    }
}