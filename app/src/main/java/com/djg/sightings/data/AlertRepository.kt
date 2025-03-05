package com.djg.sightings.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class AlertRepository(private val alertDao: AlertDao) {

    suspend fun insertAlert(alert: Alert): Long {
        return alertDao.insertAlert(alert)
    }

    suspend fun insertAlerts(alerts: List<Alert>) {
        alertDao.insertAlerts(alerts)
    }

    suspend fun updateAlert(alert: Alert) {
        alertDao.updateAlert(alert)
    }

    suspend fun deleteAlert(alert: Alert) {
        alertDao.deleteAlert(alert)
    }

    suspend fun getAllAlerts(): List<Alert> {
        return alertDao.getAllAlerts()
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    // In your repository:
    fun alertsWithinRadius(lat: Double, lon: Double, radiusKm: Double): Flow<List<Alert>> {
        val bbox = calculateBoundingBox(lat, lon, radiusKm)
        val candidateAlerts =
            alertDao.getAlertsInBoundingBox(bbox.minLat, bbox.maxLat, bbox.minLon, bbox.maxLon)
        return candidateAlerts.map { alerts ->
            alerts.filter { alert ->
                haversineDistance(lat, lon, alert.latitude, alert.longitude) < radiusKm
            }
        }
    }
}
