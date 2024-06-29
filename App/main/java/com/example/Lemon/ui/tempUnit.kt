package com.example.arcus.ui

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT;

    val unitName: String
        get() = when (this) {
            CELSIUS -> "celsius"
            FAHRENHEIT -> "fahrenheit"
        }

    companion object {
        fun fromUnitName(unitName: String): TemperatureUnit {
            return values().first { it.unitName.equals(unitName, ignoreCase = true) }
        }
    }
}
