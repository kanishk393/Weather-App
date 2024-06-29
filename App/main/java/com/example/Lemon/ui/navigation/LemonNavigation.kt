package com.example.arcus.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.arcus.domain.BriefWeatherDetails
import com.example.arcus.domain.LocationAutofillSuggestion
import com.example.arcus.ui.TemperatureUnit
import com.example.arcus.ui.home.HomeScreen
import com.example.arcus.ui.home.HomeViewModel

import com.example.arcus.ui.weatherdetail.WeatherDetailViewModel
import com.example.arcus.ui.weatherdetail.WeatherDetailScreen
import kotlinx.coroutines.launch


const val HOME_SCREEN_ROUTE = "home_screen"
const val WEATHER_DETAIL_SCREEN_ROUTE = "weather_detail/{nameOfLocation}/{latitude}/{longitude}"

@Composable
fun ArcusNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = HOME_SCREEN_ROUTE
    ) {
        homeScreen(
            route = HOME_SCREEN_ROUTE,
            onSuggestionClick = {
                navController.navigateToWeatherDetailScreen(
                    nameOfLocation = it.nameOfLocation,
                    latitude = it.coordinatesOfLocation.latitude,
                    longitude = it.coordinatesOfLocation.longitude
                )
            },
            onSavedLocationItemClick = {
                navController.navigateToWeatherDetailScreen(
                    nameOfLocation = it.nameOfLocation,
                    latitude = it.coordinates.latitude,
                    longitude = it.coordinates.longitude
                )
            }
        )

        weatherDetailScreen(
            route = WEATHER_DETAIL_SCREEN_ROUTE,
            onBackButtonClick = navController::popBackStack
        )
    }
}

private fun NavGraphBuilder.homeScreen(
    route: String,
    onSuggestionClick: (suggestion: LocationAutofillSuggestion) -> Unit,
    onSavedLocationItemClick: (BriefWeatherDetails) -> Unit
) {
    composable(route = route) {
        val viewModel = hiltViewModel<HomeViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val temperatureUnit by viewModel.temperatureUnit.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        HomeScreen(
            modifier = Modifier.fillMaxSize(),
            homeScreenUiState = uiState,
            snackbarHostState = snackbarHostState,
            onSearchQueryChange = viewModel::setSearchQueryForSuggestionsGeneration,
            onSuggestionClick = onSuggestionClick,
            onSavedLocationItemClick = onSavedLocationItemClick,
            onLocationPermissionGranted = viewModel::fetchWeatherForCurrentUserLocation,
            onRetryFetchingWeatherForSavedLocations = viewModel::retryFetchingSavedLocations,
            onTemperatureUnitChange = { unitName ->
                viewModel.setTemperatureUnit(TemperatureUnit.fromUnitName(unitName))
            },
            isFahrenheitSelected = temperatureUnit == TemperatureUnit.FAHRENHEIT
        )

    }
}

fun NavGraphBuilder.weatherDetailScreen(
    route: String,
    onBackButtonClick: () -> Unit
) {
    composable(
        route = route,
        arguments = listOf(
            navArgument("nameOfLocation") { type = NavType.StringType },
            navArgument("latitude") { type = NavType.StringType },
            navArgument("longitude") { type = NavType.StringType }
        )
    ) {
            backStackEntry ->
        val nameOfLocation = backStackEntry.arguments?.getString("nameOfLocation") ?: ""
        val latitude = backStackEntry.arguments?.getString("latitude")?:"0"
        val longitude = backStackEntry.arguments?.getString("longitude")?:"0"
        val viewModel1 = hiltViewModel<HomeViewModel>()
        val viewModel = hiltViewModel<WeatherDetailViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val temperatureUnit by viewModel1.temperatureUnit.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current

        WeatherDetailScreen(
            latitude =latitude,
            longitude = longitude,
            uiState = uiState,
            snackbarHostState = snackbarHostState,
            onBackButtonClick = onBackButtonClick,
            onSaveButtonClick = {
                viewModel.addLocationToSavedLocations()
                snackbarHostState.currentSnackbarData?.dismiss()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(message = "Added to saved locations")
                }
            },
            onTemperatureUnitChange = { unitName ->
                viewModel.setTemperatureUnit(TemperatureUnit.fromUnitName(unitName))
            },
            isFahrenheitSelected = temperatureUnit == TemperatureUnit.FAHRENHEIT,
            context = context
        )
    }
}

private fun NavHostController.navigateToWeatherDetailScreen(
    nameOfLocation: String,
    latitude: String,
    longitude: String
) {
    val destination = "weather_detail/$nameOfLocation/$latitude/$longitude"
    navigate(destination) { launchSingleTop = true }
}
