package com.example.arcus.ui.weatherdetail

import com.example.arcus.domain.CurrentWeatherDetails
import com.example.arcus.domain.HourlyForecast
import com.example.arcus.domain.PrecipitationProbability
import com.example.arcus.domain.SingleWeatherDetail

/**
 * A UI state class that represents the current UI state of the [WeatherDetailScreen].
 */
data class WeatherDetailScreenUiState(
    val isLoading: Boolean = true,
    val isPreviouslySavedLocation: Boolean = false,
    val weatherDetailsOfChosenLocation: CurrentWeatherDetails? = null,
    val isWeatherSummaryTextLoading: Boolean = false,
    val weatherSummaryText: String? = null,
    val errorMessage: String? = null,
    val precipitationProbabilities: List<PrecipitationProbability> = emptyList(),
    val hourlyForecasts: List<HourlyForecast> = emptyList(),
    val additionalWeatherInfoItems: List<SingleWeatherDetail> = emptyList()
)