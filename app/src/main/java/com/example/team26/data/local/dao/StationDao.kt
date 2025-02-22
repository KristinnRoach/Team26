package com.example.team26.data.local.dao

@Dao
interface StationDao {
    @Query("SELECT * FROM stations")
    fun getStations(): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE id = :id")
    fun getStationById(id: String): Flow<StationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<StationEntity>)

    @Update
    suspend fun updateStation(station: StationEntity)
}