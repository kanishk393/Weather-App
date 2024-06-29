package com.example.arcus.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SuggestionsResponse(@Json(name = "results") val suggestions: List<Suggestion> = emptyList()) {

    @JsonClass(generateAdapter = true)
    data class Suggestion(
        @Json(name = "id") val placeId: String,
        @Json(name = "name") val placeName: String,
        @Json(name = "country") val countryName: String?,
        @Json(name = "admin1") val stateName: String?,
        @Json(name = "country_code") val countryCode: String?,
        val latitude: String,
        val longitude: String
    ) {
        val circularCountryFlagUrl: String?
            get() = countryCode?.let { code ->
                "https://open-meteo.com/images/country-flags/$code.svg"
            }
    }
}


