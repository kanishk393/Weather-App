package com.example.arcus.ui.weatherdetail

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.example.arcus.R
import com.example.arcus.domain.HourlyForecast
import com.example.arcus.domain.PrecipitationProbability
import com.example.arcus.domain.SingleWeatherDetail
import com.example.arcus.ui.components.EnhancedLineChart
import com.example.arcus.ui.components.HourlyForecastCard
import com.example.arcus.ui.components.SingleWeatherDetailCard
import com.example.arcus.ui.components.TypingAnimatedText
import com.example.arcus.ui.fetch7DayForecast
import com.example.arcus.ui.fetchWeatherDataForMultipleYears
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



@Composable
fun WeatherDetailScreen(
    latitude: String,
    longitude: String,
    uiState: WeatherDetailScreenUiState,
    snackbarHostState: SnackbarHostState,
    onSaveButtonClick: () -> Unit,
    onBackButtonClick: () -> Unit,
    onTemperatureUnitChange: (String) -> Unit,
    isFahrenheitSelected: Boolean,
    context: Context
) {

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            content = { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }
        )
    } else if (uiState.errorMessage != null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                text = uiState.errorMessage
            )
            Button(onClick = onBackButtonClick, content = { Text("Go back") })
        }
    } else {
        WeatherDetailScreen(
            latitude =latitude,
            longitude =longitude,
            snackbarHostState = snackbarHostState,
            nameOfLocation = uiState.weatherDetailsOfChosenLocation!!.nameOfLocation,
            weatherConditionImage = uiState.weatherDetailsOfChosenLocation.imageResId,
            weatherConditionIconId = uiState.weatherDetailsOfChosenLocation.iconResId,
            weatherInDegrees = uiState.weatherDetailsOfChosenLocation.temperatureRoundedToInt.toInt(),
            weatherCondition = uiState.weatherDetailsOfChosenLocation.weatherCondition,
            aiGeneratedWeatherSummaryText = uiState.weatherSummaryText,
            isPreviouslySavedLocation = uiState.isPreviouslySavedLocation,
            isWeatherSummaryLoading = uiState.isWeatherSummaryTextLoading,
            singleWeatherDetails = uiState.additionalWeatherInfoItems,
            hourlyForecasts = uiState.hourlyForecasts,
            precipitationProbabilities = uiState.precipitationProbabilities,
            onBackButtonClick = onBackButtonClick,
            onSaveButtonClick = onSaveButtonClick,
            context = context
        )
    }
}

@Composable
fun WeatherDetailScreen(
    latitude: String,
    longitude: String,
    nameOfLocation: String,
    weatherCondition: String,
    aiGeneratedWeatherSummaryText: String?,
    @DrawableRes weatherConditionImage: Int,
    @DrawableRes weatherConditionIconId: Int,
    weatherInDegrees: Int,
    isWeatherSummaryLoading: Boolean,
    isPreviouslySavedLocation: Boolean,
    onBackButtonClick: () -> Unit,
    onSaveButtonClick: () -> Unit,
    singleWeatherDetails: List<SingleWeatherDetail>,
    hourlyForecasts: List<HourlyForecast>,
    precipitationProbabilities: List<PrecipitationProbability>,
    snackbarHostState: SnackbarHostState,
    context: Context
) {

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val sevenDayForecastData = remember { mutableStateListOf<ForecastData>() }
    val pastWeatherData = remember { mutableStateMapOf<String, PastWeatherData?>() }
    LaunchedEffect(Unit) {
        fetchWeatherDetails(latitude, longitude, sevenDayForecastData, pastWeatherData)
    }
    Log.d("LocationCoordinates", "Latitude: $latitude, Longitude: $longitude")
    pastWeatherData.forEach { (key, value) ->
        Log.d("WeatherData", "Key: $key, Value: $value")
    }
//    fetchWeatherDetails(latitude,longitude)

//        val lat = String.format("%.2f", latitude)
//        val lon = String.format("%.2f", longitude)
//
//        fetchWeatherDetails(lat, lon)
//        // Use the latitude and longitude as needed
//        Log.d("LocationCoordinates", "Latitude: $latitude, Longitude: $longitude")

    Box {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Header(
                    modifier = Modifier
                        .requiredWidth(screenWidth)
                        .height(350.dp),
                    headerImageResId = weatherConditionImage,
                    weatherConditionIconId = weatherConditionIconId,
                    onBackButtonClick = onBackButtonClick,
                    shouldDisplaySaveButton = !isPreviouslySavedLocation,
                    onSaveButtonClick = onSaveButtonClick,
                    nameOfLocation = nameOfLocation,
                    currentWeatherInDegrees = weatherInDegrees,
                    weatherCondition = weatherCondition
                )
            }

            if (aiGeneratedWeatherSummaryText != null || isWeatherSummaryLoading) {
                item {
                    WeatherSummaryTextCard(
                        summaryText = aiGeneratedWeatherSummaryText ?: "",
                        isWeatherSummaryLoading = isWeatherSummaryLoading
                    )
                }
            }

            item {
                HourlyForecastCard(hourlyForecasts = hourlyForecasts, precipitationProbabilities = precipitationProbabilities)
            }
            item {
                SevenDayForecastCard1(sevenDayForecastData = sevenDayForecastData)
            }

            item {
                PastWeatherLineChart(pastWeatherData = pastWeatherData)
            }

            items(singleWeatherDetails) { detail ->
                SingleWeatherDetailCard(
                    name = detail.name,
                    value = detail.value,
                    iconResId = detail.iconResId
                )
            }

            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }

        SnackbarHost(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            hostState = snackbarHostState
        )
    }
}


@Composable
fun SevenDayForecastCard1(sevenDayForecastData: List<ForecastData>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "7-Day Forecast",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow {
                items(sevenDayForecastData) { day ->
                    ForecastItem(day)
                }
            }
        }
    }
}

@Composable
fun ForecastItem(day: ForecastData) {
    Column(
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = day.datetime,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Max: ${day.tempmax}, Min: ${day.tempmin}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = day.conditions,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


//@Composable
//fun WeatherDetailScreen(
//    latitude: String,
//    longitude: String,
//    nameOfLocation: String,
//    weatherCondition: String,
//    aiGeneratedWeatherSummaryText: String?,
//    @DrawableRes weatherConditionImage: Int,
//    @DrawableRes weatherConditionIconId: Int,
//    weatherInDegrees: Int,
//    isWeatherSummaryLoading: Boolean,
//    isPreviouslySavedLocation: Boolean,
//    onBackButtonClick: () -> Unit,
//    onSaveButtonClick: () -> Unit,
//    singleWeatherDetails: List<SingleWeatherDetail>,
//    hourlyForecasts: List<HourlyForecast>,
//    precipitationProbabilities: List<PrecipitationProbability>,
//    snackbarHostState: SnackbarHostState,
//    context: Context
//) {
//    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
//    val sevenDayForecastData = remember { mutableStateListOf<ForecastData>() }
//    val pastWeatherData = remember { mutableStateMapOf<String, PastWeatherData?>() }
//
//    LaunchedEffect(Unit) {
//        fetchWeatherDetails(latitude, longitude, sevenDayForecastData, pastWeatherData)
//    }
//
//    // ... (existing code)
//
//    Box {
//        LazyColumn(
//            modifier = Modifier.fillMaxSize(),
//            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // ... (existing items)
//
//            item {
//                SevenDayForecastCard(sevenDayForecastData = sevenDayForecastData)
//            }
//
//            item {
//                PastWeatherDataCard(pastWeatherData = pastWeatherData)
//            }
//
//            // ... (existing items)
//        }
//
//        // ... (existing code)
//    }
//}

@Composable
fun SevenDayForecastList(sevenDayForecastData: List<ForecastData>) {
    LazyColumn {
        item {
            SevenDayForecastCard(sevenDayForecastData = sevenDayForecastData)
        }
    }
}

@Composable
fun SevenDayForecastCard(sevenDayForecastData: List<ForecastData>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "7-Day Forecast",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            sevenDayForecastData.forEach { day ->
                DayForecastItem(day = day)
            }
        }
    }
}

@Composable
fun DayForecastItem(day: ForecastData) {
    Column {
        Text(
            text = "Date: ${day.datetime}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Max Temp: ${day.tempmax}, Min Temp: ${day.tempmin}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Conditions: ${day.conditions}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}


@Composable
fun PastWeatherLineChart(
    pastWeatherData: Map<String, PastWeatherData?>,
    modifier: Modifier = Modifier,
    lineColorMax: Color = Color.Red,
    lineColorMin: Color = Color.Blue,
    textColor: Color = Color.Black
) {
    val maxTemps = mutableListOf<Float>()
    val minTemps = mutableListOf<Float>()
    val dates = mutableListOf<String>()

    pastWeatherData.forEach { (date, data) ->
        if (data != null) {
            maxTemps.add(data.tempmax.toFloat())
            minTemps.add(data.tempmin.toFloat())
            dates.add(date)
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text("Maximum Temperature Over 10 Years", style = MaterialTheme.typography.headlineSmall)
        EnhancedLineChart(
            dataPoints = maxTemps,
            timePoints = dates, // Format the time in 24-hour format
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Minimum Temperature Over 10 Years", style = MaterialTheme.typography.headlineSmall)
        EnhancedLineChart(
            dataPoints = minTemps,
            timePoints = dates, // Format the time in 24-hour format
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
        )
    }
}




data class ForecastData(
    val datetime: String,
    val tempmax: Double,
    val tempmin: Double,
    val humidity: Double,
    val precipprob: Double,
    val conditions: String
)

data class PastWeatherData(
    val tempmax: Double,
    val tempmin: Double
)

fun fetchWeatherDetails(
    latitude: String,
    longitude: String,
    sevenDayForecastData: MutableList<ForecastData>,
    pastWeatherData: MutableMap<String, PastWeatherData?>
) {
    CoroutineScope(Dispatchers.IO).launch {
        val currentDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = currentDateFormat.format(Date())

        // Fetch 7-day forecast
        fetch7DayForecast(latitude, longitude, currentDate) { forecastData ->
            sevenDayForecastData.clear()
            sevenDayForecastData.addAll(
                forecastData.map { day ->
                    ForecastData(
                        datetime = day.datetime,
                        tempmax = day.tempmax,
                        tempmin = day.tempmin,
                        humidity = day.humidity,
                        precipprob = day.precipprob,
                        conditions = day.conditions
                    )
                }
            )
        }

        // Fetch weather data for the past 10 years
        fetchWeatherDataForMultipleYears(latitude, longitude, currentDate) { pastWeatherDataList ->
            pastWeatherData.clear()
            pastWeatherData.putAll(
                pastWeatherDataList.associate { (date, data) ->
                    date to data?.let {
                        PastWeatherData(
                            tempmax = it.tempmax,
                            tempmin = it.tempmin
                        )
                    }
                }
            )
        }
    }
}

//
//data class DailyForecast(
//    val datetime: String,
//    val tempmax: Double,
//    val tempmin: Double,
//    val humidity: Int,
//    val precipprob: Int,
//    val conditions: String
//)
//
//data class YearlyWeatherData(
//    val date: String,
//    val tempmax: Double?,
//    val tempmin: Double?
//)
//
//data class ForecastData(
//    val date: String,
//    val maxTemp: Double,
//    val minTemp: Double,
//    val humidity: Int,
//    val precipProbability: Int,
//    val conditions: String
//)
//
//data class PastWeatherData(
//    val date: String,
//    val maxTemp: Double?,
//    val minTemp: Double?
//)
//@Composable
//fun ForecastCard(forecastData: ForecastData) {
//    Card(modifier = Modifier.fillMaxWidth()) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(text = "Date: ${forecastData.date}", style = MaterialTheme.typography.h6)
//            Text(text = "Max Temp: ${forecastData.maxTemp}°, Min Temp: ${forecastData.minTemp}°")
//            Text(text = "Humidity: ${forecastData.humidity}%")
//            Text(text = "Precip Probability: ${forecastData.precipProbability}%")
//            Text(text = "Conditions: ${forecastData.conditions}")
//        }
//    }
//}

//@Composable
//fun PastWeatherCard(pastWeatherData: PastWeatherData) {
//    Card(modifier = Modifier.fillMaxWidth()) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(text = "Date: ${pastWeatherData.date}", style = MaterialTheme.typography.h6)
//            Text(text = "Max Temp: ${pastWeatherData.maxTemp ?: "N/A"}°, Min Temp: ${pastWeatherData.minTemp ?: "N/A"}°")
//        }
//    }
//}



//fun fetchWeatherDetails(latitude: String, longitude: String) {
//    CoroutineScope(Dispatchers.IO).launch {
//        val currentDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        val currentDate = currentDateFormat.format(Date())
//
//        // Fetch 7-day forecast
//        fetch7DayForecast(latitude, longitude, currentDate) { forecastData ->
//            Log.d("WeatherDetails", "7-Day Forecast:")
//            forecastData.forEach { day ->
//                Log.d("WeatherDetails", "Date: ${day.datetime}, Max Temp: ${day.tempmax}, Min Temp: ${day.tempmin}, Humidity: ${day.humidity}%, Precip Probability: ${day.precipprob}%, Conditions: ${day.conditions}")
//            }
//        }
//
//        // Fetch weather data for the past 10 years
//        fetchWeatherDataForMultipleYears(latitude, longitude, currentDate) { pastWeatherData ->
//            Log.d("WeatherDetails", "Past 10 Years Data:")
//            pastWeatherData.forEach { (date, data) ->
//                data?.let {
//                    Log.d("WeatherDetails", "Date: $date, Max Temp: ${it.tempmax}, Min Temp: ${it.tempmin}")
//                } ?: Log.d("WeatherDetails", "Date: $date, Data: Not Available")
//            }
//        }
//    }
//}


@Composable
private fun Header(
    modifier: Modifier = Modifier,
    @DrawableRes headerImageResId: Int,
    @DrawableRes weatherConditionIconId: Int,
    onBackButtonClick: () -> Unit,
    shouldDisplaySaveButton: Boolean,
    onSaveButtonClick: () -> Unit,
    nameOfLocation: String,
    currentWeatherInDegrees: Int,
    weatherCondition: String,
) {
    Box(modifier = modifier) {
        val iconButtonContainerColor = remember {
            Color.Black.copy(0.4f)
        }
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = headerImageResId),
            contentScale = ContentScale.Crop,
            contentDescription = null
        )
        //scrim for image
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
        )

        IconButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding(),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = iconButtonContainerColor
            ),
            onClick = onBackButtonClick
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = null
            )
        }
        if (shouldDisplaySaveButton) {
            IconButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding(),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = iconButtonContainerColor
                ),
                onClick = onSaveButtonClick
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                text = nameOfLocation,
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$currentWeatherInDegrees°",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp)
            )
            Row(
                modifier = Modifier.offset(x = (-8).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Explicitly set tint to Color.Unspecified to ensure that no tint is applied to the vector
                // resource. See documentation of the Icon composable for more information.
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = ImageVector.vectorResource(id = weatherConditionIconId),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Text(text = weatherCondition)
            }
        }
    }
}


fun getCoordinatesFromLocationName(context: Context, locationName: String): Pair<Double, Double>? {
    val geocoder = Geocoder(context)
    try {
        val addresses: MutableList<Address>? = geocoder.getFromLocationName(locationName, 1)
        if (addresses != null) {
            if (addresses.isNotEmpty()) {
                val address: Address = addresses[0]
                val latitude: Double = address.latitude
                val longitude: Double = address.longitude
                return Pair(latitude, longitude)
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}


@Composable
private fun WeatherSummaryTextCard(
    modifier: Modifier = Modifier,
    isWeatherSummaryLoading: Boolean,
    summaryText: String,
) {
    Card(modifier = modifier) {
        val context = LocalContext.current
        val imageLoader = remember {
            ImageLoader.Builder(context = context)
                .components {
                    if (SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }
                .build()
        }
        val imageRequest = remember {
            ImageRequest.Builder(context = context)
                .data(R.drawable.bard_sparkle_thinking_anim)
                .size(Size.ORIGINAL)
                .build()
        }
        val asyncImagePainter = rememberAsyncImagePainter(
            model = imageRequest,
            imageLoader = imageLoader
        )

        Row(
            modifier = Modifier.padding(top = 8.dp, start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isWeatherSummaryLoading) {
                Image(
                    modifier = Modifier.size(16.dp),
                    painter = asyncImagePainter,
                    contentDescription = null
                )
            } else {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = ImageVector.vectorResource(R.drawable.ic_bard_logo),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            }
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleMedium
            )
        }
        TypingAnimatedText(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            text = summaryText
        )
    }
}