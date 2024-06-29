package com.example.arcus.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

data class WeatherData(
    val days: List<DayData>
)

data class DayData(
    val datetime: String,
    val tempmax: Double,
    val tempmin: Double,
    val humidity: Double,
    val precipprob: Double,
    val conditions: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                WeatherApp()
            }
        }
    }
}

@Composable
fun WeatherApp() {
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var pastWeatherData by remember { mutableStateOf<List<Pair<String, DayData?>>?>(null) }
    var forecastData by remember { mutableStateOf<List<DayData>?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = latitude,
            onValueChange = { latitude = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter latitude") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = longitude,
            onValueChange = { longitude = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter longitude") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = date,
            onValueChange = { date = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter date (YYYY-MM-DD)") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                isLoading = true
                fetchWeatherDataForMultipleYears(latitude, longitude, date) { data ->
                    pastWeatherData = data
                    isLoading = false
                }
            }
        ) {
            Text("Fetch Data for Past 10 Years")
        }
        Button(
            onClick = {
                isLoading = true
                fetch7DayForecast(latitude, longitude, date) { data ->
                    forecastData = data
                    isLoading = false
                }
            }
        ) {
            Text("Fetch 7-Day Forecast")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) {
            Text("Loading...")
        } else {
            pastWeatherData?.forEach { (date, data) ->
                data?.let {
                    Text("Date: $date, Max Temp: ${it.tempmax}, Min Temp: ${it.tempmin}")
                }
            }
            forecastData?.forEach { day ->
                Text("Date: ${day.datetime}, Max Temp: ${day.tempmax}, Min Temp: ${day.tempmin}, Humidity: ${day.humidity}%, Precip Probability: ${day.precipprob}%, Conditions: ${day.conditions}")
            }
        }
    }
}

fun fetchWeatherDataForMultipleYears(latitude: String, longitude: String, inputDate: String, onSuccess: (List<Pair<String, DayData?>>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val results = mutableListOf<Pair<String, DayData?>>()
        val year = inputDate.substring(0, 4).toInt()
        val monthDay = inputDate.substring(5)
        for (y in year - 1 downTo year - 10) {
            val dateString = "$y-$monthDay"
            val urlString = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/$latitude,$longitude/$dateString?unitGroup=metric&include=days&key=53JDYAVTLYK2ZPGUHLSZRZEMA&contentType=json"
            try {
                val url = URL(urlString)
                val connection = url.openConnection()
                connection.connect()
                val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                val weatherData = Gson().fromJson(responseString, WeatherData::class.java)
                val dayData = weatherData.days.firstOrNull()
                results.add(Pair(dateString, dayData))
            } catch (e: Exception) {
                println("Error fetching weather data for $dateString: ${e.message}")
                results.add(Pair(dateString, null))
            }
        }
        onSuccess(results)
    }
}

fun fetch7DayForecast(latitude: String, longitude: String, inputDate: String, onSuccess: (List<DayData>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance().apply {
            time = dateFormat.parse(inputDate) ?: Date()
        }
        val startDate = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_MONTH, 6)
        val endDate = dateFormat.format(cal.time)
        val urlString = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/$latitude,$longitude/$startDate/$endDate?unitGroup=metric&include=days&key=53JDYAVTLYK2ZPGUHLSZRZEMA&contentType=json"
        try {
            val url = URL(urlString)
            val connection = url.openConnection()
            connection.connect()
            val responseString = connection.inputStream.bufferedReader().use { it.readText() }
            val weatherData = Gson().fromJson(responseString, WeatherData::class.java)
            onSuccess(weatherData.days)
        } catch (e: Exception) {
            println("Error fetching 7-day forecast: ${e.message}")
            onSuccess(emptyList())
        }
    }
}