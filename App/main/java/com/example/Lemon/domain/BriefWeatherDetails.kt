package com.example.arcus.domain

import androidx.annotation.DrawableRes
import com.example.arcus.domain.Coordinates


data class BriefWeatherDetails(
    val nameOfLocation: String,
    val currentTemperatureRoundedToInt: Int,
    val shortDescription: String,
    @DrawableRes val shortDescriptionIcon: Int,
    val coordinates: Coordinates
)

