package com.example.arcus.domain

import com.example.arcus.data.SuggestionsResponse

data class LocationAutofillSuggestion(
    val idOfLocation: String,
    val nameOfLocation: String,
    val addressOfLocation: String,
    val coordinatesOfLocation: Coordinates,
    val countryFlagUrl: String
)

fun List<SuggestionsResponse.Suggestion>.toLocationAutofillSuggestionList(): List<LocationAutofillSuggestion> =
    this.filter { it.stateName != null && it.countryName != null && it.circularCountryFlagUrl != null }
        .map {
            LocationAutofillSuggestion(
                idOfLocation = it.placeId,
                nameOfLocation = it.placeName,
                addressOfLocation = "${it.stateName}, ${it.countryName}",
                coordinatesOfLocation = Coordinates(
                    latitude = it.latitude,
                    longitude = it.longitude
                ),
                countryFlagUrl = it.circularCountryFlagUrl!!
            )
        }
