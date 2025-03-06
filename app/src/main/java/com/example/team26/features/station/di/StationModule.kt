package com.example.team26.features.station.di

import com.example.team26.features.station.data.repository.MockStationRepository
import com.example.team26.features.station.domain.repository.StationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for station-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class StationModule {

    @Binds
    @Singleton
    abstract fun bindStationRepository(
        mockRepository: MockStationRepository
    ): StationRepository
}
