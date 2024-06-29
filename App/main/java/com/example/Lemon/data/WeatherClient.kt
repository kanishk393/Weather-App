package com.example.arcus.data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.LocalDate
import java.time.ZoneId


interface WeatherClient {

    @GET("forecast")
    suspend fun getWeatherForCoordinates(
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String,
        @Query("temperature_unit") temperatureUnit: String, // Removed default value to allow dynamic input
        @Query("windspeed_unit") windSpeedUnit: String = "kmh",
        @Query("precipitation_unit") precipitationUnit: String = "inch",
        @Query("current_weather") shouldIncludeCurrentWeatherInformation: Boolean = true // must always be set to true
    ): Response<CurrentWeatherResponse>




    @GET("forecast")
    suspend fun getHourlyForecast(
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String,
        @Query("start_date") startDate: LocalDate,
        @Query("end_date") endDate: LocalDate,
        @Query("timezone") timezoneConfiguration: String = ZoneId.systemDefault().toString(),
        @Query("precipitation_unit") precipitationUnit: String = "inch",
        @Query("timeformat") timeFormat: String = "unixtime",
        @Query("hourly") hourlyForecastsToReturn: String = "weathercode,precipitation_probability,temperature_2m"
    ): Response<HourlyWeatherInfoResponse>



    @GET("forecast")
    suspend fun getAdditionalDailyForecastVariables(
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String,
        @Query("start_date") startDate: LocalDate,
        @Query("end_date") endDate: LocalDate,
        @Query("timezone") timezoneConfiguration: String = "auto",
        @Query("timeformat") timeFormat: String = "unixtime",
        @Query("daily") dailyForecastsToReturn: String = "temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,sunrise,sunset,uv_index_max,windspeed_10m_max,winddirection_10m_dominant"
    ): Response<AdditionalDailyForecastVariablesResponse>

}


