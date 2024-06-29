package com.example.arcus.ui.home

import com.example.arcus.domain.BriefWeatherDetails
import com.example.arcus.domain.HourlyForecast
import com.example.arcus.domain.LocationAutofillSuggestion


data class HomeScreenUiState(
    val isLoadingAutofillSuggestions: Boolean = false,
    val isLoadingSavedLocations: Boolean = false,
    val isLoadingWeatherDetailsOfCurrentLocation: Boolean = false,
    val errorFetchingWeatherForCurrentLocation: Boolean = false,
    val errorFetchingWeatherForSavedLocations: Boolean = false,
    val errorFetchingAutofillSuggestions: Boolean = false,
    val weatherDetailsOfCurrentLocation: BriefWeatherDetails? = null,
    val hourlyForecastsForCurrentLocation: List<HourlyForecast>? = null,
    val autofillSuggestions: List<LocationAutofillSuggestion> = emptyList(),
    val weatherDetailsOfSavedLocations: List<BriefWeatherDetails> = emptyList(),
)