package com.example.arcus.ui.weatherdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arcus.data.ArcusDatabaseDao
import com.example.arcus.data.SavedWeatherLocationEntity
import com.example.arcus.data.WeatherClient
import com.example.arcus.data.getBodyOrThrowException
import com.example.arcus.domain.CurrentWeatherDetails
import com.example.arcus.domain.toCurrentWeatherDetails
import com.example.arcus.domain.toHourlyForecasts
import com.example.arcus.domain.toPrecipitationProbabilities
import com.example.arcus.domain.toSingleWeatherDetailList
import com.example.arcus.ui.TemperatureUnit
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject




@HiltViewModel
class WeatherDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val weatherClient: WeatherClient,
    private val arcusDatabaseDao: ArcusDatabaseDao
) : ViewModel() {
    // Define keys for navigation arguments
    companion object {
        private const val NAV_ARG_NAME_OF_LOCATION = "nameOfLocation"
        const val NAV_ARG_LATITUDE = "latitude"
        const val NAV_ARG_LONGITUDE = "longitude"
        private const val DEFAULT_ERROR_MESSAGE = "Oops! An error occurred when trying to fetch the weather details. Please try again."
    }

    private val latitude: String = savedStateHandle[NAV_ARG_LATITUDE]!!
    private val longitude: String = savedStateHandle[NAV_ARG_LONGITUDE]!!
    private val nameOfLocation: String = savedStateHandle[NAV_ARG_NAME_OF_LOCATION]!!
    private val _uiState = MutableStateFlow(WeatherDetailScreenUiState())
    val uiState: StateFlow<WeatherDetailScreenUiState> = _uiState.asStateFlow()

    private val _temperatureUnit = MutableStateFlow(TemperatureUnit.CELSIUS)
    val temperatureUnit: StateFlow<TemperatureUnit> = _temperatureUnit.asStateFlow()

    init {
        arcusDatabaseDao.getAllWeatherEntitiesMarkedAsNotDeleted()
            .map { namesOfSavedLocationsList ->
                namesOfSavedLocationsList.any { it.nameOfLocation == nameOfLocation }
            }
            .onEach { isPreviouslySavedLocation ->
                _uiState.update { it.copy(isPreviouslySavedLocation = isPreviouslySavedLocation) }
            }
            .launchIn(scope = viewModelScope)

        viewModelScope.launch {
            try {
                fetchWeatherDetailsAndUpdateState()
            } catch (exception: Exception) {
                if (exception is CancellationException) throw exception
                _uiState.update { it.copy(isLoading = false, errorMessage = DEFAULT_ERROR_MESSAGE) }
            }
        }
    }

    private suspend fun fetchWeatherDetailsAndUpdateState() = coroutineScope {
        _uiState.update { it.copy(isLoading = true, isWeatherSummaryTextLoading = true) }

        val weatherResponse = weatherClient.getWeatherForCoordinates(
            latitude = latitude,
            longitude = longitude,
            temperatureUnit = temperatureUnit.value.toString().lowercase()
        ).getBodyOrThrowException()
        val weatherDetails = weatherResponse.toCurrentWeatherDetails(nameOfLocation)

        val summaryMessage = generateTextForWeatherDetails(weatherDetails)

        val precipitationProbabilities = async {
            weatherClient.getHourlyForecast(
                latitude = latitude,
                longitude = longitude,
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(1)
            ).getBodyOrThrowException().toPrecipitationProbabilities().take(24)
        }

        val hourlyForecasts = async {
            weatherClient.getHourlyForecast(
                latitude = latitude,
                longitude = longitude,
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(1)
            ).getBodyOrThrowException().toHourlyForecasts().take(24)
        }

        val additionalWeatherInfoItems = async {
            weatherClient.getAdditionalDailyForecastVariables(
                latitude = latitude,
                longitude = longitude,
                startDate = LocalDate.now(),
                endDate = LocalDate.now()
            ).getBodyOrThrowException().toSingleWeatherDetailList()
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                weatherDetailsOfChosenLocation = weatherDetails,
                precipitationProbabilities = precipitationProbabilities.await(),
                hourlyForecasts = hourlyForecasts.await(),
                additionalWeatherInfoItems = additionalWeatherInfoItems.await(),
                isWeatherSummaryTextLoading = false,
                weatherSummaryText = summaryMessage
            )
        }
    }

    private suspend fun generateTextForWeatherDetails(weatherDetails: CurrentWeatherDetails): String {
        val prompt = """
        Generate a short description Majorly it should tell us about pattern of weather and any alerts or is it a good day to go and any alerts if any.
        location = ${weatherDetails.nameOfLocation};
        currentTemperature = ${weatherDetails.temperatureRoundedToInt};
        weatherCondition = ${weatherDetails.weatherCondition};
        isNight = ${weatherDetails.isDay != 1}
    """.trimIndent()

        val defaultErrorMessage = "Sorry, I'm having trouble responding to you. Please try again."
        val apiKey = ""
        val generativeModel = GenerativeModel(modelName = "gemini-pro", apiKey = apiKey)

        return try {
            generativeModel.generateContent(prompt).text ?: defaultErrorMessage
        } catch (exception: Exception) {
            println(exception)
            if (exception is CancellationException) throw exception
            defaultErrorMessage
        }
    }

    fun addLocationToSavedLocations() {
        viewModelScope.launch {
            val savedWeatherEntity = SavedWeatherLocationEntity(
                nameOfLocation = nameOfLocation,
                latitude = latitude,
                longitude = longitude
            )
            arcusDatabaseDao.addSavedWeatherEntity(savedWeatherEntity)
        }
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        _temperatureUnit.value = unit
    }
}
