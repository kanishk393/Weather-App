package com.example.arcus.domain

import androidx.annotation.DrawableRes
import com.example.arcus.R
import com.example.arcus.data.AdditionalDailyForecastVariablesResponse
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


data class SingleWeatherDetail(
    val name: String,
    val value: String,
    @DrawableRes val iconResId: Int
)



fun AdditionalDailyForecastVariablesResponse.toSingleWeatherDetailList(
    timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("hh : mm a")
): List<SingleWeatherDetail> = additionalForecastedVariables.toSingleWeatherDetailList(
    timezone = timezone,
    timeFormat = timeFormat
)


private fun AdditionalDailyForecastVariablesResponse.AdditionalForecastedVariables.toSingleWeatherDetailList(
    timezone: String,
    timeFormat: DateTimeFormatter
): List<SingleWeatherDetail> {
    require(minTemperatureForTheDay.size == 1) {
        "This mapper method will only consider the first value of each list" +
                "Make sure you request the details for only one day."
    }
    val apparentTemperature =
        (minApparentTemperature.first().roundToInt() + maxApparentTemperature.first()
            .roundToInt()) / 2
    val sunriseTimeString = LocalDateTime.ofInstant(
        Instant.ofEpochSecond(sunrise.first()),
        ZoneId.of(timezone)
    ).toLocalTime().format(timeFormat)
    val sunsetTimeString = LocalDateTime.ofInstant(
        Instant.ofEpochSecond(sunset.first()),
        ZoneId.of(timezone)
    ).format(timeFormat)
    return listOf(
        SingleWeatherDetail(
            name = "Min Temp",
            value = "${minTemperatureForTheDay.first().roundToInt()}º",
            iconResId = R.drawable.baseline_thermostat_24
        ),
        SingleWeatherDetail(
            name = "Max Temp",
            value = "${maxTemperatureForTheDay.first().roundToInt()}º",
            iconResId = R.drawable.baseline_thermostat_24
        ),
        SingleWeatherDetail(
            name = "Sunrise",
            value = sunriseTimeString,
            iconResId = R.drawable.sunrise_svgrepo_com
        ),
        SingleWeatherDetail(
            name = "Sunset",
            value = sunsetTimeString,
            iconResId = R.drawable.sunset_svgrepo_com
        ),
        SingleWeatherDetail(
            name = "Feels Like",
            value = "${apparentTemperature}º",
            iconResId = R.drawable.baseline_thermostat_24
        ),
        SingleWeatherDetail(
            name = "Max UV Index",
            value = maxUvIndex.first().toString(),
            iconResId = R.drawable.uv_index_alt_svgrepo_com
        ),
        SingleWeatherDetail(
            name = "Wind Direction",
            value = "${dominantWindDirection.first()}º",
            iconResId = R.drawable.direction_wind_speed_navigation_svgrepo_com
        ),
        SingleWeatherDetail(
            name = "Wind Speed",
            value = "${windSpeed.first()} Km/h",
            iconResId = R.drawable.windfinder_svgrepo_com
        )
    )
}