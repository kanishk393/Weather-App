package com.example.arcus.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arcus.data.ArcusDatabaseDao
import com.example.arcus.data.ReverseGeocoder

import com.example.arcus.domain.CurrentLocationProvider
import com.example.arcus.domain.BriefWeatherDetails
import com.example.arcus.domain.CurrentWeatherDetails
import com.example.arcus.domain.SavedLocation
import com.example.arcus.domain.toBriefWeatherDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.arcus.data.WeatherClient
import com.example.arcus.data.getBodyOrThrowException
import com.example.arcus.data.LocationClient
import com.example.arcus.domain.LocationAutofillSuggestion
import com.example.arcus.domain.toLocationAutofillSuggestionList
import com.example.arcus.domain.toCurrentWeatherDetails
import com.example.arcus.domain.toHourlyForecasts
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

import com.example.arcus.domain.toSavedLocation
import com.example.arcus.ui.TemperatureUnit

import kotlinx.coroutines.flow.map


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val currentLocationProvider: CurrentLocationProvider,
    private val reverseGeocoder: ReverseGeocoder,
    private val weatherClient: WeatherClient,
    private val arcusDatabaseDao: ArcusDatabaseDao,
    private val locationClient: LocationClient
) : ViewModel() {

    private val currentSearchQuery = MutableStateFlow("")
    private val isCurrentlyRetryingToFetchSavedLocation = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState = _uiState as StateFlow<HomeScreenUiState>

    private val _temperatureUnit = MutableStateFlow(TemperatureUnit.CELSIUS)
    val temperatureUnit: StateFlow<TemperatureUnit> = _temperatureUnit.asStateFlow()

    // a cache that stores the CurrentWeatherDetails of a specific SavedLocation
    private var currentWeatherDetailsCache = mutableMapOf<SavedLocation, CurrentWeatherDetails>()

    init {
        // saved locations stream
        combine(
            arcusDatabaseDao.getAllWeatherEntitiesMarkedAsNotDeleted().map { savedLocationEntitiesList ->
                savedLocationEntitiesList.map { it.toSavedLocation() }
            },
            isCurrentlyRetryingToFetchSavedLocation
        ) { savedLocations, _ ->
            savedLocations
        }.onEach {
            _uiState.update {
                it.copy(
                    isLoadingSavedLocations = true,
                    errorFetchingWeatherForSavedLocations = false
                )
            }
        }.map { savedLocations ->
            fetchCurrentWeatherDetailsWithCache(savedLocations)
        }.onEach { weatherDetailsOfSavedLocationsResult ->
            val weatherDetailsOfSavedLocations =
                weatherDetailsOfSavedLocationsResult.getOrNull()
            _uiState.update {
                it.copy(
                    isLoadingSavedLocations = false,
                    weatherDetailsOfSavedLocations = weatherDetailsOfSavedLocations ?: emptyList(),
                    errorFetchingWeatherForSavedLocations = weatherDetailsOfSavedLocations == null
                )
            }
            isCurrentlyRetryingToFetchSavedLocation.update { false }
        }.launchIn(viewModelScope)

        // suggestions for current search query stream
        @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
        currentSearchQuery.debounce(250)
            .distinctUntilChanged()
            .mapLatest { query ->
                if (query.isBlank()) {
                    Result.success(emptyList())
                } else {
                    _uiState.update {
                        it.copy(
                            isLoadingAutofillSuggestions = true,
                            errorFetchingAutofillSuggestions = false
                        )
                    }
                    try {
                        val suggestions = locationClient.getPlacesSuggestionsForQuery(query = query)
                            .getBodyOrThrowException()
                            .suggestions
                            .toLocationAutofillSuggestionList()
                        Result.success(suggestions)
                    } catch (e: Throwable) {
                        if (e is CancellationException) throw e
                        Result.failure<List<LocationAutofillSuggestion>>(e)
                    }
                }
            }
            .onEach { autofillSuggestionsResult ->
                val autofillSuggestions = autofillSuggestionsResult.getOrNull()
                _uiState.update {
                    it.copy(
                        isLoadingAutofillSuggestions = false,
                        autofillSuggestions = autofillSuggestions ?: emptyList(),
                        errorFetchingAutofillSuggestions = autofillSuggestions == null
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun retryFetchingSavedLocations() {
        val isCurrentlyRetrying = isCurrentlyRetryingToFetchSavedLocation.value
        if (isCurrentlyRetrying) return
        isCurrentlyRetryingToFetchSavedLocation.update { true }
    }

    fun setSearchQueryForSuggestionsGeneration(searchQuery: String) {
        currentSearchQuery.value = searchQuery
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        _temperatureUnit.value = unit
    }

    private suspend fun fetchCurrentWeatherDetailsWithCache(savedLocations: List<SavedLocation>): Result<List<BriefWeatherDetails>?> {
        val savedLocationsSet = savedLocations.toSet()
        // remove locations in the cache that have been deleted by the user
        val removedLocations = currentWeatherDetailsCache.keys subtract savedLocationsSet
        for (removedLocation in removedLocations) {
            currentWeatherDetailsCache.remove(removedLocation)
        }
        // only fetch weather details of the items that are not in cache.
        val locationsNotInCache = savedLocationsSet subtract currentWeatherDetailsCache.keys
        for (savedLocationNotInCache in locationsNotInCache) {
            try {
                // Directly call the weather client here instead of using the repository
                val response = weatherClient.getWeatherForCoordinates(
                    latitude = savedLocationNotInCache.coordinates.latitude,
                    longitude = savedLocationNotInCache.coordinates.longitude,
                    temperatureUnit = temperatureUnit.value.toString().lowercase()
                )
                // Process the response and add it to the cache
                response.getBodyOrThrowException().toCurrentWeatherDetails(savedLocationNotInCache.nameOfLocation).also {
                    currentWeatherDetailsCache[savedLocationNotInCache] = it
                }
            } catch (exception: Exception) {
                if (exception is CancellationException) throw exception
                return Result.failure(exception)
            }
        }
        return Result.success(
            currentWeatherDetailsCache.values.toList().map { it.toBriefWeatherDetails() }
        )
    }

    fun fetchWeatherForCurrentUserLocation() {
        val exceptionHandler = CoroutineExceptionHandler { _, _ ->
            _uiState.update {
                it.copy(
                    isLoadingWeatherDetailsOfCurrentLocation = false,
                    errorFetchingWeatherForCurrentLocation = true
                )
            }
        }
        viewModelScope.launch(exceptionHandler) {
            _uiState.update {
                it.copy(
                    isLoadingWeatherDetailsOfCurrentLocation = true,
                    errorFetchingWeatherForCurrentLocation = false
                )
            }

            val coordinates = currentLocationProvider.getCurrentLocation().getOrThrow()
            val nameOfLocation = reverseGeocoder.getLocationNameForCoordinates(
                coordinates.latitude.toDouble(),
                coordinates.longitude.toDouble()
            ).getOrThrow()

            val weatherDetailsForCurrentLocation = async {
                try {
                    // Directly call the weather client here instead of using the repository
                    val response = weatherClient.getWeatherForCoordinates(
                        latitude = coordinates.latitude,
                        longitude = coordinates.longitude,
                        temperatureUnit = temperatureUnit.value.toString().lowercase()
                    )
                    // Process the response and convert it to brief weather details
                    Result.success(response.getBodyOrThrowException().toCurrentWeatherDetails(nameOfLocation)).getOrThrow().toBriefWeatherDetails()
                } catch (exception: Exception) {
                    if (exception is CancellationException) throw exception
                    // Handle failure case by re-throwing or logging the exception as needed
                    throw exception
                }
            }

            val hourlyForecastsForCurrentLocation = async {
                try {
                    // Fetch the raw hourly forecasts data
                    val rawForecasts = weatherClient.getHourlyForecast(
                        latitude = coordinates.latitude,
                        longitude = coordinates.longitude,
                        startDate = LocalDate.now(),
                        endDate = LocalDate.now().plusDays(1),
                        timezoneConfiguration = ZoneId.systemDefault().toString(),
                        precipitationUnit = "inch",
                        timeFormat = "unixtime",
                        hourlyForecastsToReturn = "weathercode,precipitation_probability,temperature_2m"
                    ).getBodyOrThrowException().toHourlyForecasts()

                    // Filter and process the forecasts to get the next 24 hours starting from now
                    val filteredForecasts = rawForecasts.filter {
                        val isSameDay = it.dateTime.toLocalDate() == LocalDate.now()
                        if (isSameDay) it.dateTime.toLocalTime() >= LocalTime.now()
                        else it.dateTime.toLocalDate() > LocalDate.now()
                    }.take(24)

                    filteredForecasts
                } catch (exception: Exception) {
                    if (exception is CancellationException) throw exception
                    null
                }
            }.await()

            _uiState.update {
                it.copy(
                    isLoadingWeatherDetailsOfCurrentLocation = false,
                    errorFetchingWeatherForCurrentLocation = false,
                    weatherDetailsOfCurrentLocation = weatherDetailsForCurrentLocation.await(),
                    hourlyForecastsForCurrentLocation = hourlyForecastsForCurrentLocation,
                )
            }
        }
    }
}
