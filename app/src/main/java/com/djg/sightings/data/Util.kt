package com.djg.sightings.data

fun calculateBoundingBox(lat: Double, lon: Double, radiusKm: Double): BoundingBox {
    // Approximate degrees per km at given latitude
    val latDegreeKm = 111.32
    val deltaLat = radiusKm / latDegreeKm

    // Adjust longitude degrees based on latitude (cosine of latitude)
    val lonDegreeKm = 111.32 * kotlin.math.cos(Math.toRadians(lat))
    val deltaLon = radiusKm / lonDegreeKm

    return BoundingBox(
        minLat = lat - deltaLat,
        maxLat = lat + deltaLat,
        minLon = lon - deltaLon,
        maxLon = lon + deltaLon
    )
}

data class BoundingBox(
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double
)
