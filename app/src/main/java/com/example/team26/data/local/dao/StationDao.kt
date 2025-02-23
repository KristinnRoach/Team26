package com.example.team26.data.local.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.team26.domain.model.Station
import kotlinx.coroutines.flow.Flow


@Dao
interface StationDao {
    @Query("SELECT * FROM stations")
    fun getStations(): Flow<List<Station>>

    @Query("SELECT * FROM stations WHERE id = :id")
    fun getStationById(id: String): Flow<Station?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stationEntities: List<Station>)

    @Update
    suspend fun updateStation(stationEntity: Station)
}