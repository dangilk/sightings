package com.djg.sightings.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun Context.locationFlow(): Flow<Location> = callbackFlow {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@locationFlow)

    val locationRequest = LocationRequest.Builder(
        5000, // Desired interval in milliseconds for active location updates
        Priority.PRIORITY_HIGH_ACCURACY.toLong()
    )
        .setMinUpdateIntervalMillis(3000L) // Fastest rate for active location updates
        .build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            // Emit each location update
            result.locations.forEach { location ->
                trySend(location).isSuccess
            }
        }
    }

    if (ActivityCompat.checkSelfPermission(
            this@locationFlow,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }


    // Remove location updates when the flow collector is cancelled
    awaitClose {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
