package com.example.arcus.domain

/**
 * A provider that provides the current location of the device.
 */
fun interface CurrentLocationProvider {

    suspend fun getCurrentLocation(): Result<Coordinates>
}
