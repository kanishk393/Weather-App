package com.example.arcus.ui.home

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.arcus.domain.BriefWeatherDetails
import com.example.arcus.domain.HourlyForecast
import com.example.arcus.domain.LocationAutofillSuggestion
import com.example.arcus.ui.TemperatureUnit
import com.example.arcus.ui.activities.NotificationActivity
import com.example.arcus.ui.components.AutofillSuggestion
//import com.example.arcus.ui.components.CompactWeatherCardWithHourlyForecast
import com.example.arcus.ui.components.WeatherCard

import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.placeholder
import com.google.accompanist.placeholder.material3.shimmer


@Composable
fun HomeScreen(
    homeScreenUiState: HomeScreenUiState,
    snackbarHostState: SnackbarHostState,
    onSearchQueryChange: (String) -> Unit,
    onSuggestionClick: (LocationAutofillSuggestion) -> Unit,
    onSavedLocationItemClick: (BriefWeatherDetails) -> Unit,
    onLocationPermissionGranted: () -> Unit,
    onRetryFetchingWeatherForSavedLocations: () -> Unit,
    onRetryFetchingWeatherForCurrentLocation: () -> Unit = onLocationPermissionGranted,
    onTemperatureUnitChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isFahrenheitSelected: Boolean
) {
    val context = LocalContext.current

    var isSearchBarActive by remember { mutableStateOf(false) }
    var currentQueryText by remember { mutableStateOf("") }
    val clearQueryText = {
        currentQueryText = ""
        onSearchQueryChange("")
    }
    var shouldDisplayCurrentLocationWeatherSubHeader by remember { mutableStateOf(false) }

    var isFahrenheitSelected by remember { mutableStateOf(false) }

    handlePermissions(onLocationPermissionGranted = {
        shouldDisplayCurrentLocationWeatherSubHeader = true
        onLocationPermissionGranted()
    })

    fun startNotificationActivity(weatherDetails: BriefWeatherDetails) {
        val intent = Intent(context, NotificationActivity::class.java).apply {
            putExtra("nameOfLocation", weatherDetails.nameOfLocation)
            putExtra("currentTemperatureRoundedToInt", weatherDetails.currentTemperatureRoundedToInt)
            putExtra("shortDescription", weatherDetails.shortDescription)
        }
        context.startActivity(intent)
    }


    Box {
        HomeScreenLazyColumn(
            modifier = modifier,
            homeScreenUiState = homeScreenUiState,
            onSavedLocationItemClick = { weatherDetails ->
                onSavedLocationItemClick(weatherDetails)
                startNotificationActivity(weatherDetails)
            },
            onRetryFetchingWeatherForSavedLocations = onRetryFetchingWeatherForSavedLocations,
            onRetryFetchingWeatherForCurrentLocation = onRetryFetchingWeatherForCurrentLocation,
            currentQueryText = currentQueryText,
            onSearchQueryChange = {
                currentQueryText = it
                onSearchQueryChange(it)
            },
            isSearchBarActive = isSearchBarActive,
            onSearchBarActiveChange = { isSearchBarActive = it },
            clearQueryText = clearQueryText,
            onSuggestionClick = onSuggestionClick,
            shouldDisplayCurrentLocationWeatherSubHeader = shouldDisplayCurrentLocationWeatherSubHeader,
            isFahrenheitSelected = isFahrenheitSelected,
            onTemperatureUnitChange = { isSelected ->
                val newUnit = if (isSelected) TemperatureUnit.FAHRENHEIT else TemperatureUnit.CELSIUS
                isFahrenheitSelected = isSelected
                onTemperatureUnitChange(newUnit.unitName)
            }


        )

        HomeScreenSnackbarHost(snackbarHostState = snackbarHostState)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
private fun HomeScreenLazyColumn(
    modifier: Modifier,
    homeScreenUiState: HomeScreenUiState,
    onSavedLocationItemClick: (BriefWeatherDetails) -> Unit,
    onRetryFetchingWeatherForSavedLocations: () -> Unit,
    onRetryFetchingWeatherForCurrentLocation: () -> Unit,
    currentQueryText: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchBarActive: Boolean,
    onSearchBarActiveChange: (Boolean) -> Unit,
    clearQueryText: () -> Unit,
    onSuggestionClick: (LocationAutofillSuggestion) -> Unit,
    shouldDisplayCurrentLocationWeatherSubHeader: Boolean,
    isFahrenheitSelected: Boolean,
    onTemperatureUnitChange: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = WindowInsets.navigationBars.asPaddingValues()
    ) {
        searchBarItem(
            currentSearchQuery = currentQueryText,
            onClearSearchQueryIconClick = clearQueryText,
            isSearchBarActive = isSearchBarActive,
            errorLoadingSuggestions = homeScreenUiState.errorFetchingAutofillSuggestions,
            onSearchQueryChange = onSearchQueryChange,
            onSearchBarActiveChange = onSearchBarActiveChange,
            suggestionsForSearchQuery = homeScreenUiState.autofillSuggestions,
            isSuggestionsListLoading = homeScreenUiState.isLoadingAutofillSuggestions,
            onSuggestionClick = onSuggestionClick
        )

        item {
            TemperatureUnitToggle(
                isFahrenheitSelected = isFahrenheitSelected,
                onToggleChange = onTemperatureUnitChange
            )
        }

        if (shouldDisplayCurrentLocationWeatherSubHeader) {
            subHeaderItem(
                title = "Current Location",
                isLoadingAnimationVisible = homeScreenUiState.isLoadingWeatherDetailsOfCurrentLocation
            )
        }

        homeScreenUiState.weatherDetailsOfCurrentLocation?.let { currentWeather ->
            homeScreenUiState.hourlyForecastsForCurrentLocation?.let { hourlyForecasts ->
                currentWeatherDetailCardItem(
                    weatherOfCurrentUserLocation = currentWeather,
                    hourlyForecastsOfCurrentUserLocation = hourlyForecasts,
                    onClick = { onSavedLocationItemClick(currentWeather) }
                )
            }
        }

        if (homeScreenUiState.errorFetchingWeatherForCurrentLocation) {
            errorCardItem(
                modifier = Modifier.padding(horizontal = 16.dp),
                errorMessage = "An error occurred when fetching the weather for the current location.",
                onRetryButtonClick = onRetryFetchingWeatherForCurrentLocation
            )
        }

        subHeaderItem(
            title = "Saved Locations",
            isLoadingAnimationVisible = homeScreenUiState.isLoadingSavedLocations
        )

        if (homeScreenUiState.errorFetchingWeatherForSavedLocations) {
            errorCardItem(
                modifier = Modifier.padding(horizontal = 16.dp),
                errorMessage = "An error occurred when fetching the current weather details of saved locations.",
                onRetryButtonClick = onRetryFetchingWeatherForSavedLocations
            )
        }

        savedLocationItems(
            savedLocationItemsList = homeScreenUiState.weatherDetailsOfSavedLocations,
            onSavedLocationItemClick = onSavedLocationItemClick
        )
    }
}

@Composable
fun TemperatureUnitToggle(
    isFahrenheitSelected: Boolean,
    onToggleChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "°C")
        Switch(
            checked = isFahrenheitSelected,
            onCheckedChange = onToggleChange
        )
        Text(text = "°F")
    }
}





@Composable
private fun handlePermissions(
    onLocationPermissionGranted: () -> Unit
) {
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { isPermitted ->
            val isLocationPermitted = isPermitted.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) ||
                    isPermitted.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false)
            if (isLocationPermitted) {
                onLocationPermissionGranted()
            }
        }
    )

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }
}

//@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
//    ExperimentalAnimationApi::class
//)
//@Composable
//private fun HomeScreenLazyColumn(
//    modifier: Modifier,
//    homeScreenUiState: HomeScreenUiState,
//    onSavedLocationItemClick: (BriefWeatherDetails) -> Unit,
//    onRetryFetchingWeatherForSavedLocations: () -> Unit,
//    onRetryFetchingWeatherForCurrentLocation: () -> Unit,
//    currentQueryText: String,
//    onSearchQueryChange: (String) -> Unit,
//    isSearchBarActive: Boolean,
//    onSearchBarActiveChange: (Boolean) -> Unit,
//    clearQueryText: () -> Unit,
//    onSuggestionClick: (LocationAutofillSuggestion) -> Unit,
//    onTemperatureUnitChange: (Boolean) -> Unit,
//    shouldDisplayCurrentLocationWeatherSubHeader: Boolean,
//    isFahrenheitSelected: Boolean
//
//) {
//    LazyColumn(
//        modifier = modifier,
//        verticalArrangement = Arrangement.spacedBy(16.dp),
//        contentPadding = WindowInsets.navigationBars.asPaddingValues()
//    ) {
//        searchBarItem(
//            currentSearchQuery = currentQueryText,
//            onClearSearchQueryIconClick = clearQueryText,
//            isSearchBarActive = isSearchBarActive,
//            errorLoadingSuggestions = homeScreenUiState.errorFetchingAutofillSuggestions,
//            onSearchQueryChange = onSearchQueryChange,
//            onSearchBarActiveChange = onSearchBarActiveChange,
//            suggestionsForSearchQuery = homeScreenUiState.autofillSuggestions,
//            isSuggestionsListLoading = homeScreenUiState.isLoadingAutofillSuggestions,
//            onSuggestionClick = onSuggestionClick
//        )
//        item {
//            TemperatureUnitToggle(
//                isFahrenheitSelected = isFahrenheitSelected,
//                onToggleChange = onTemperatureUnitChange
//            )
//        }
//
//
//        if (shouldDisplayCurrentLocationWeatherSubHeader) {
//            subHeaderItem(
//                title = "Current Location",
//                isLoadingAnimationVisible = homeScreenUiState.isLoadingWeatherDetailsOfCurrentLocation
//            )
//        }
//
//        homeScreenUiState.weatherDetailsOfCurrentLocation?.let { currentWeather ->
//            homeScreenUiState.hourlyForecastsForCurrentLocation?.let { hourlyForecasts ->
//                currentWeatherDetailCardItem(
//                    weatherOfCurrentUserLocation = currentWeather,
//                    hourlyForecastsOfCurrentUserLocation = hourlyForecasts,
//                    onClick = { onSavedLocationItemClick(currentWeather) }
//                )
//            }
//        }
//
//        if (homeScreenUiState.errorFetchingWeatherForCurrentLocation) {
//            errorCardItem(
//                modifier = Modifier.padding(horizontal = 16.dp),
//                errorMessage = "An error occurred when fetching the weather for the current location.",
//                onRetryButtonClick = onRetryFetchingWeatherForCurrentLocation
//            )
//        }
//
//        subHeaderItem(
//            title = "Saved Locations",
//            isLoadingAnimationVisible = homeScreenUiState.isLoadingSavedLocations
//        )
//
//        if (homeScreenUiState.errorFetchingWeatherForSavedLocations) {
//            errorCardItem(
//                modifier = Modifier.padding(horizontal = 16.dp),
//                errorMessage = "An error occurred when fetching the current weather details of saved locations.",
//                onRetryButtonClick = onRetryFetchingWeatherForSavedLocations
//            )
//        }
//
//        savedLocationItems(
//            savedLocationItemsList = homeScreenUiState.weatherDetailsOfSavedLocations,
//            onSavedLocationItemClick = onSavedLocationItemClick,
//
//        )
//    }
//}

@Composable
private fun HomeScreenSnackbarHost(snackbarHostState: SnackbarHostState) {
    SnackbarHost(
        modifier = Modifier

            .navigationBarsPadding(),
        hostState = snackbarHostState
    )
}

//@Composable
//fun TemperatureUnitToggle(
//    isFahrenheitSelected: Boolean,
//    onToggleChange: (Boolean) -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        horizontalArrangement = Arrangement.Center,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(text = "°C")
//        Switch(
//            checked = isFahrenheitSelected,
//            onCheckedChange = onToggleChange
//        )
//        Text(text = "°F")
//    }
//}




@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@Composable
private fun Header(
    modifier: Modifier = Modifier,
    currentSearchQuery: String,
    onClearSearchQueryIconClick: () -> Unit,
    isSearchBarActive: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSearchBarActiveChange: (Boolean) -> Unit,
    searchBarSuggestionsContent: @Composable (ColumnScope.() -> Unit)
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    Column(modifier = modifier) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .sizeIn(
                    maxWidth = screenWidth, // see docs for explanation
                    maxHeight = screenHeight // see docs for explanation
                ),
            query = currentSearchQuery,
            onQueryChange = onSearchQueryChange,
            onSearch = {
                // no need for this callback because this app uses instant search
            },
            active = isSearchBarActive,
            onActiveChange = onSearchBarActiveChange,
            leadingIcon = {
                AnimatedSearchBarLeadingIcon(
                    isSearchBarActive = isSearchBarActive,
                    onSearchIconClick = { onSearchBarActiveChange(true) },
                    onBackIconClick = {
                        // clear search query text when clicking on the back button
                        onClearSearchQueryIconClick()
                        onSearchBarActiveChange(false)
                    }
                )
            },
            placeholder = { Text(text = "Search for a location") },
            content = searchBarSuggestionsContent
        )
    }
}


@ExperimentalAnimationApi
@Composable
private fun AnimatedSearchBarLeadingIcon(
    isSearchBarActive: Boolean,
    onSearchIconClick: () -> Unit,
    onBackIconClick: () -> Unit
) {
    AnimatedContent(
        targetState = isSearchBarActive,
        transitionSpec = {
            val isActive = this.targetState
            val slideIn = slideIntoContainer(
                towards = if (isActive) AnimatedContentTransitionScope.SlideDirection.Start
                else AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
            val slideOut = slideOutOfContainer(
                towards = if (isActive) AnimatedContentTransitionScope.SlideDirection.End
                else AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
            slideIn togetherWith slideOut
        },
        label = "SearchBarIconTransition"
    ) { isActive ->
        IconButton(
            onClick = if (isActive) onBackIconClick else onSearchIconClick,
            modifier = Modifier.graphicsLayer {
                rotationZ = if (isActive) 180f else 0f
            }
        ) {
            Icon(
                imageVector = if (isActive) Icons.Filled.ArrowBack else Icons.Filled.Search,
                contentDescription = if (isActive) "Back" else "Search"
            )
        }
    }
}


@Composable
private fun AutoFillSuggestionsList(
    suggestions: List<LocationAutofillSuggestion>,
    isSuggestionsListLoading: Boolean,
    onSuggestionClick: (LocationAutofillSuggestion) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isSuggestionsListLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn {
                autofillSuggestionItems(
                    suggestions = suggestions,
                    onSuggestionClick = onSuggestionClick
                )
                item {
                    Spacer(modifier = Modifier.imePadding())
                }
            }
        }
    }
}

private fun LazyListScope.autofillSuggestionItems(
    suggestions: List<LocationAutofillSuggestion>,
    onSuggestionClick: (LocationAutofillSuggestion) -> Unit,
) {
    items(items = suggestions, key = { it.idOfLocation }) {
        AutofillSuggestion(
            title = it.nameOfLocation,
            subText = it.addressOfLocation,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            onClick = { onSuggestionClick(it) },
            leadingIcon = { AutofillSuggestionLeadingIcon(countryFlagUrl = it.countryFlagUrl) }
        )
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
private fun LazyListScope.savedLocationItems(
    savedLocationItemsList: List<BriefWeatherDetails>,
    onSavedLocationItemClick: (BriefWeatherDetails) -> Unit,

    ) {
    items(
        items = savedLocationItemsList,
        key = { it.nameOfLocation } // swipeable cards will be buggy without keys
    ) {

        WeatherCard(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .animateItemPlacement(),
            nameOfLocation = it.nameOfLocation,
            shortDescription = it.shortDescription,
            shortDescriptionIcon = it.shortDescriptionIcon,
            weatherInDegrees = it.currentTemperatureRoundedToInt.toString(),
            onClick = { onSavedLocationItemClick(it) }
        )
    }
}

@ExperimentalMaterial3Api
@ExperimentalAnimationApi
private fun LazyListScope.searchBarItem(
    currentSearchQuery: String,
    isSearchBarActive: Boolean,
    isSuggestionsListLoading: Boolean,
    errorLoadingSuggestions: Boolean,
    suggestionsForSearchQuery: List<LocationAutofillSuggestion>,
    onClearSearchQueryIconClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchBarActiveChange: (Boolean) -> Unit,
    onSuggestionClick: (LocationAutofillSuggestion) -> Unit
) {
    item {
        val searchBarSuggestionsContent = @Composable {
            AutoFillSuggestionsList(
                suggestions = suggestionsForSearchQuery,
                onSuggestionClick = onSuggestionClick,
                isSuggestionsListLoading = isSuggestionsListLoading
            )
        }
        val errorSearchBarSuggestionsContent = @Composable {
            OutlinedCard(modifier = Modifier.padding(16.dp)) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    text = "An error occurred when fetching the suggestions. Please retype to try again."
                )
            }
        }

        Header(
            modifier = Modifier.fillMaxWidth(),
            currentSearchQuery = currentSearchQuery,
            onClearSearchQueryIconClick = onClearSearchQueryIconClick,
            isSearchBarActive = isSearchBarActive,
            onSearchQueryChange = onSearchQueryChange,
            onSearchBarActiveChange = onSearchBarActiveChange,
            searchBarSuggestionsContent = {
                if (errorLoadingSuggestions) errorSearchBarSuggestionsContent()
                else searchBarSuggestionsContent()
            }
        )
    }
}

private fun LazyListScope.subHeaderItem(title: String, isLoadingAnimationVisible: Boolean) {
    item {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .padding(end = 8.dp),
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Normal
            )
            if (isLoadingAnimationVisible) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
private fun LazyListScope.currentWeatherDetailCardItem(
    weatherOfCurrentUserLocation: BriefWeatherDetails,
    hourlyForecastsOfCurrentUserLocation: List<HourlyForecast>,
    onClick: () -> Unit,
) {
    item {
        WeatherCard(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .animateItemPlacement(),
            nameOfLocation = weatherOfCurrentUserLocation.nameOfLocation,
            weatherInDegrees = weatherOfCurrentUserLocation.currentTemperatureRoundedToInt.toString(),
            hourlyForecasts = hourlyForecastsOfCurrentUserLocation,
            onClick = onClick
        )
    }
}

@Composable
private fun AutofillSuggestionLeadingIcon(countryFlagUrl: String) {
    val context = LocalContext.current
    val imageRequest = remember(countryFlagUrl) {
        ImageRequest.Builder(context)
            .data(countryFlagUrl)
            .decoderFactory(SvgDecoder.Factory())
            .build()
    }
    var isLoadingAnimationVisible by remember { mutableStateOf(false) }
    AsyncImage(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .placeholder(
                visible = isLoadingAnimationVisible,
                highlight = PlaceholderHighlight.shimmer()
            ),
        model = imageRequest,
        contentDescription = null,
        onState = { asyncPainterState ->
            isLoadingAnimationVisible = asyncPainterState is AsyncImagePainter.State.Loading
        }
    )
}

@ExperimentalFoundationApi
private fun LazyListScope.errorCardItem(
    errorMessage: String,
    modifier: Modifier = Modifier,
    retryButtonText: String = "Retry",
    onRetryButtonClick: () -> Unit
) {
    item {
        OutlinedCard(modifier = modifier.animateItemPlacement()) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = errorMessage,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(16.dp))
                OutlinedButton(
                    onClick = onRetryButtonClick,
                    content = { Text(text = retryButtonText) })
            }
        }
    }
}
