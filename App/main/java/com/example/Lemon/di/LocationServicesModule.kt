package com.example.arcus.di

import com.example.arcus.data.ArcusReverseGeocoder
import com.example.arcus.data.ReverseGeocoder
import com.example.arcus.domain.CurrentLocationProvider
import com.example.arcus.domain.ArcusCurrentLocationProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class LocationServicesModule {

    @Binds
    abstract fun bindCurrentLocationProvider(impl: ArcusCurrentLocationProvider): CurrentLocationProvider

    @Binds
    abstract fun bindReverseGeocoder(impl: ArcusReverseGeocoder): ReverseGeocoder
}