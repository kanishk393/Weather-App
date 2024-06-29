package com.example.arcus.domain

import androidx.annotation.DrawableRes
import com.example.arcus.data.HourlyWeatherInfoResponse
import com.example.arcus.data.getWeatherResourceForCode
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.roundToInt

data class HourlyForecast(
    val dateTime: LocalDateTime,
    @DrawableRes val weatherIconResId: Int,
    val temperature: Int
)


fun HourlyWeatherInfoResponse.toHourlyForecasts(): List<HourlyForecast> {
    val hourlyForecastList = mutableListOf<HourlyForecast>()
    for (i in hourlyForecast.timestamps.indices) {
        val epochSeconds = hourlyForecast.timestamps[i].toLong()
        val correspondingLocalTime = LocalDateTime
            .ofInstant(
                Instant.ofEpochSecond(epochSeconds),
                ZoneId.systemDefault()
            )
        val weatherIconResId = getWeatherResourceForCode(
            weatherCode = hourlyForecast.weatherCodes[i],
            isDay = correspondingLocalTime.hour < 19,
            isIcon = true
        )
        val hourlyForecast = HourlyForecast(
            dateTime = correspondingLocalTime,
            weatherIconResId = weatherIconResId,
            temperature = hourlyForecast.temperatureForecasts[i].roundToInt()
        )
        hourlyForecastList.add(hourlyForecast)
    }
    return hourlyForecastList
}

