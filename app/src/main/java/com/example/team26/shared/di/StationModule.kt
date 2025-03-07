package com.example.team26.shared.di

// Just keeping for reference -kiddi





/**

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.team26.shared.data.station.MockStationRepositoryImpl
import com.example.team26.shared.domain.station.StationRepository
import javax.inject.Singleton

// Dagger Hilt module for station-related dependencies

@Module
@InstallIn(SingletonComponent::class)
abstract class StationModule {

    @Binds
    @Singleton
    abstract fun bindStationRepository(
        mockRepository: MockStationRepositoryImpl
    ): StationRepository
}
 */