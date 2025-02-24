package com.example.team26.features.station.data.local.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.team26.features.station.data.local.entity.StationEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface StationDao {
    @Query("SELECT * FROM stations")
    fun getAllStations(): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE id = :id")
    fun getStationById(id: String): Flow<StationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stationEntities: List<StationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: StationEntity)

    @Update
    suspend fun updateStation(stationEntity: StationEntity)
}