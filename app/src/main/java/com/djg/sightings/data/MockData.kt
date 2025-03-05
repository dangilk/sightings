package com.djg.sightings.data

import com.djg.sightings.service.LocationService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class MockData(
    private val locationService: LocationService,
    private val alertRepository: AlertRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun scheduleMockAlerts() {
        withContext(ioDispatcher) {
            locationService.locationFlow.collect { location ->
                while (true) {
                    alertRepository.insertAlerts(
                        createMockAlerts(
                            centerLat = location.latitude,
                            centerLon = location.longitude,
                            count = 1,
                            radius = 40,
                        )
                    )
                    delay(1000)
                }
            }
        }
    }
}

fun createMockAlerts(
    centerLat: Double,
    centerLon: Double,
    radius: Long = 40,
    count: Int = 10
): List<Alert> {
    val random = Random.Default
    val alerts = mutableListOf<Alert>()

    // Earth's radius in kilometers for reference in degree conversion (approximate)
    // 1 degree latitude is roughly 111.32 km
    val kmPerDegree = 111.32

    for (i in 1..count) {
        // Generate a random distance (in km) uniformly within the circle.
        // Taking sqrt(random) ensures uniform distribution across the circle area.
        val distance = radius * sqrt(random.nextDouble())

        // Random angle in radians (0 to 2π)
        val angle = random.nextDouble() * 2 * Math.PI

        // Calculate offsets in degrees
        val deltaLat = (distance * cos(angle)) / kmPerDegree
        // Adjust longitude conversion by the cosine of the center latitude (in radians)
        val deltaLon = (distance * sin(angle)) / (kmPerDegree * cos(Math.toRadians(centerLat)))

        // New coordinates for the alert
        val lat = centerLat + deltaLat
        val lon = centerLon + deltaLon

        val date = System.currentTimeMillis()

        val isRead = false

        // Create and add the Alert object
        alerts.add(
            Alert(
                title = "Alert! ($lat, $lon)",
                date = date,
                latitude = lat,
                longitude = lon,
                isRead = isRead
            )
        )
    }
    return alerts
}