package com.example.team26.shared.di

// Just keeping for reference -kiddi

/*
import com.example.team26.shared.api.StationApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideStationApi(): StationApi =
        Retrofit.Builder()
            .baseUrl("https://your-api-base-url/")
            .build()
            .create(StationApi::class.java)

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            // .addInterceptor(AuthInterceptor())
            .build()
    }
}

 */