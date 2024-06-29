package com.example.arcus.data

import com.example.arcus.R
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherData(
    val latitude: String? = null,
    val longitude: String? = null,
    val currentTemperature: Double? = null,
    val isDay: Int? = null,
    val currentWeatherCode: Int? = null,
    val timezone: String? = null,
    val minTemperatureForTheDay: List<Double>? = null,
    val maxTemperatureForTheDay: List<Double>? = null,
    val maxApparentTemperature: List<Double>? = null,
    val minApparentTemperature: List<Double>? = null,
    val sunrise: List<Long>? = null,
    val sunset: List<Long>? = null,
    val maxUvIndex: List<Double>? = null,
    val dominantWindDirection: List<Int>? = null,
    val maxWindSpeed: List<Double>? = null,
    val timestamps: List<String>? = null,
    val precipitationProbabilityPercentages: List<Int>? = null,
    val hourlyWeatherCodes: List<Int>? = null,
    val temperatureForecasts: List<Float>? = null
)


@JsonClass(generateAdapter = true)
data class CurrentWeatherResponse(
    @Json(name = "current_weather") val currentWeather: CurrentWeather,
    val latitude: String,
    val longitude: String,
    @Json(name = "cloud_cover") var cloudCover: Double? = null,
    @Json(name = "visibility") var visibility: Int? = null,
    @Json(name = "humidity") var humidity: Double? = null,
    @Json(name = "pressure") var pressure: Double? = null,
    @Json(name = "ozone_level") var ozoneLevel: Double? = null,
    @Json(name = "air_quality_index") var airQualityIndex: Int? = null,
    @Json(name = "dew_point") var dewPoint: Double? = null,
    @Json(name = "uv_index") var uvIndex: Double? = null,
    @Json(name = "soil_moisture") var soilMoisture: Double? = null,
    @Json(name = "soil_temperature") var soilTemperature: Double? = null,
    @Json(name = "storm_distance") var stormDistance: Double? = null,
    @Json(name = "storm_direction") var stormDirection: Int? = null
) {
    @JsonClass(generateAdapter = true)
    data class CurrentWeather(
        val temperature: Double,
        @Json(name = "is_day") val isDay: Int,
        @Json(name = "weathercode") val weatherCode: Int,
        @Json(name = "feels_like_temperature") var feelsLikeTemperature: Double? = null,
        @Json(name = "wind_chill") var windChill: Double? = null,
        @Json(name = "heat_index") var heatIndex: Double? = null,
        @Json(name = "wind_speed") var windSpeed: Double? = null,
        @Json(name = "wind_gust") var windGust: Double? = null,
        @Json(name = "precipitation_rate") var precipitationRate: Double? = null,
        @Json(name = "precipitation_type") var precipitationType: Int? = null,
        @Json(name = "snow_depth") var snowDepth: Double? = null,
        @Json(name = "frost_point") var frostPoint: Double? = null,
        @Json(name = "ice_point") var icePoint: Double? = null,
        @Json(name = "thunder_probability") var thunderProbability: Int? = null
    )
}

@JsonClass(generateAdapter = true)
data class AdditionalDailyForecastVariablesResponse(
    @Json(name = "timezone") val timezone: String,
    @Json(name = "daily") val additionalForecastedVariables: AdditionalForecastedVariables,
    @Json(name = "moon_phase") var moonPhase: String? = null,
    @Json(name = "astronomical_twilight_begin") var astronomicalTwilightBegin: Long? = null,
    @Json(name = "astronomical_twilight_end") var astronomicalTwilightEnd: Long? = null,
    @Json(name = "nautical_twilight_begin") var nauticalTwilightBegin: Long? = null,
    @Json(name = "nautical_twilight_end") var nauticalTwilightEnd: Long? = null,
    @Json(name = "civil_twilight_begin") var civilTwilightBegin: Long? = null,
    @Json(name = "civil_twilight_end") var civilTwilightEnd: Long? = null,
    @Json(name = "solar_noon") var solarNoon: Long? = null,
    @Json(name = "day_length") var dayLength: Long? = null,
    @Json(name = "sun_altitude") var sunAltitude: Double? = null,
    @Json(name = "sun_azimuth") var sunAzimuth: Double? = null,
    @Json(name = "solar_radiation") var solarRadiation: Double? = null,
    @Json(name = "solar_uv_index") var solarUvIndex: Double? = null
) {

    @JsonClass(generateAdapter = true)
    data class AdditionalForecastedVariables(
        @Json(name = "temperature_2m_min") val minTemperatureForTheDay: List<Double>,
        @Json(name = "temperature_2m_max") val maxTemperatureForTheDay: List<Double>,
        @Json(name = "apparent_temperature_max") val maxApparentTemperature: List<Double>,
        @Json(name = "apparent_temperature_min") val minApparentTemperature: List<Double>,
        @Json(name = "sunrise") val sunrise: List<Long>,
        @Json(name = "sunset") val sunset: List<Long>,
        @Json(name = "uv_index_max") val maxUvIndex: List<Double>,
        @Json(name = "winddirection_10m_dominant") val dominantWindDirection: List<Int>,
        @Json(name = "windspeed_10m_max") val windSpeed: List<Double>,
        @Json(name = "precipitation_accumulation") var precipitationAccumulation: List<Double>? = null,
        @Json(name = "snowfall") var snowfall: List<Double>? = null,
        @Json(name = "fog_probability") var fogProbability: List<Int>? = null,
        @Json(name = "cloud_cover_percentage") var cloudCoverPercentage: List<Double>? = null,
        @Json(name = "visibility_average") var visibilityAverage: List<Double>? = null,
        @Json(name = "lightning_strike_count") var lightningStrikeCount: List<Int>? = null
    )
}

@JsonClass(generateAdapter = true)
data class HourlyWeatherInfoResponse(
    val latitude: String,
    val longitude: String,
    @Json(name = "hourly") val hourlyForecast: HourlyForecast,
    @Json(name = "sea_level_pressure") var seaLevelPressure: List<Double>? = null,
    @Json(name = "ground_level_pressure") var groundLevelPressure: List<Double>? = null,
    @Json(name = "humidity_percentage") var humidityPercentage: List<Double>? = null,
    @Json(name = "dew_point_temperature") var dewPointTemperature: List<Double>? = null,
    @Json(name = "ozone_concentration") var ozoneConcentration: List<Double>? = null,
    @Json(name = "particulate_matter_2_5") var particulateMatter25: List<Double>? = null,
    @Json(name = "particulate_matter_10") var particulateMatter10: List<Double>? = null,
    @Json(name = "nitrogen_dioxide_level") var nitrogenDioxideLevel: List<Double>? = null,
    @Json(name = "sulfur_dioxide_level") var sulfurDioxideLevel: List<Double>? = null,
    @Json(name = "carbon_monoxide_level") var carbonMonoxideLevel: List<Double>? = null,
    @Json(name = "volatile_organic_compounds") var volatileOrganicCompounds: List<Double>? = null
) {

    @JsonClass(generateAdapter = true)
    data class HourlyForecast(
        @Json(name = "time") val timestamps: List<String>,
        @Json(name = "precipitation_probability") val precipitationProbabilityPercentages: List<Int> = emptyList(),
        @Json(name = "weathercode") val weatherCodes: List<Int> = emptyList(),
        @Json(name = "temperature_2m") val temperatureForecasts: List<Float> = emptyList(),
        @Json(name = "wind_direction_degrees") var windDirectionDegrees: List<Int>? = null,
        @Json(name = "wind_speed_knots") var windSpeedKnots: List<Double>? = null,
        @Json(name = "gust_speed_knots") var gustSpeedKnots: List<Double>? = null,
        @Json(name = "wave_height") var waveHeight: List<Double>? = null,
        @Json(name = "wave_period") var wavePeriod: List<Double>? = null,
        @Json(name = "wave_direction") var waveDirection: List<Int>? = null,
        @Json(name = "sea_surface_temperature") var seaSurfaceTemperature: List<Double>? = null,
        @Json(name = "water_salinity") var waterSalinity: List<Double>? = null,
        @Json(name = "water_ph_level") var waterPhLevel: List<Double>? = null
    )
}



fun getWeatherResourceForCode(weatherCode: Int, isDay: Boolean, isIcon: Boolean): Int {
    val partlyCloudyCodes = listOf(1, 2, 3)
    val precipitationCodes = listOf(51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82, 95, 96, 99)
    val winterPrecipitationCodes = listOf(71, 73, 75, 77, 85, 86)
    val lowVisibilityCodes = listOf(45, 48)
    val thunderstormCodes = listOf(61, 63, 65, 66, 67, 95, 96, 99)

    return if (isDay) {
        when (weatherCode) {
            0 -> if (isIcon) R.drawable.ic_day_clear else R.drawable.img_day_clear
            in partlyCloudyCodes -> if (isIcon) R.drawable.ic_day_few_clouds else R.drawable.img_day_cloudy
            in precipitationCodes -> if (isIcon) {
                if (weatherCode in thunderstormCodes) R.drawable.ic_day_thunderstorms
                else R.drawable.ic_day_rain
            } else R.drawable.img_day_rain
            in winterPrecipitationCodes -> if (isIcon) R.drawable.ic_day_snow else R.drawable.img_day_snow
            in lowVisibilityCodes -> if (isIcon) R.drawable.ic_mist else R.drawable.img_day_fog
            else -> throw IllegalArgumentException("Unknown weatherCode $weatherCode")
        }
    } else {
        when (weatherCode) {
            0 -> if (isIcon) R.drawable.ic_night_clear else R.drawable.img_night_clear
            in partlyCloudyCodes -> if (isIcon) R.drawable.ic_night_few_clouds else R.drawable.img_night_cloudy
            in precipitationCodes -> if (isIcon) {
                if (weatherCode in thunderstormCodes) R.drawable.ic_night_thunderstorms
                else R.drawable.ic_night_rain
            } else R.drawable.img_night_rain
            in winterPrecipitationCodes -> if (isIcon) R.drawable.ic_night_snow else R.drawable.img_night_snow
            in lowVisibilityCodes -> if (isIcon) R.drawable.ic_mist else R.drawable.img_night_fog
            else -> throw IllegalArgumentException("Unknown weatherCode $weatherCode")
        }
    }
}
