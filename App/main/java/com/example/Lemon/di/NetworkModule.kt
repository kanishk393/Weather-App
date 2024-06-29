package com.example.arcus.di

import com.example.arcus.data.LocationClient
import com.example.arcus.data.WeatherClient

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideWeatherClient(): WeatherClient = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/v1/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(WeatherClient::class.java)

    @Provides
    @Singleton
    fun provideLocationClient(): LocationClient = Retrofit.Builder()
        .baseUrl("https://geocoding-api.open-meteo.com/v1/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(LocationClient::class.java)



}