package com.example.arcus.data

import com.squareup.moshi.JsonClass

fun List<String>.toDoublePairOrNull(): Pair<Double, Double>? {
    if (size != 2) return null
    return try {
        get(0).toDouble() to get(1).toDouble()
    } catch (e: NumberFormatException) {
        null
    }
}

@JsonClass(generateAdapter = true)
data class CoordinatesResponse(val features: List<Feature>) {
    @JsonClass(generateAdapter = true)
    data class Feature(val geometry: Geometry) {
        @JsonClass(generateAdapter = true)
        data class Geometry(val coordinates: List<String>)
    }

    data class Coordinates(val longitude: String, val latitude: String)

    val coordinates: Coordinates?
        get() = features.firstOrNull()?.geometry?.coordinates
            ?.toDoublePairOrNull()
            ?.let { (longitude, latitude) -> Coordinates(longitude.toString(), latitude.toString()) }
}