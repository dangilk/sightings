package com.djg.sightings.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest

class LocationService(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val _locationPermissionState = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val locationFlow = _locationPermissionState.filter { it }.flatMapLatest { permissionGranted ->
        if (permissionGranted) {
            _getLocationFlow()
        } else {
            emptyFlow()
        }
    }

    init {
        checkLocationPermission()
    }

    fun startLocationUpdates() {
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        // Request location updates with the location request and callback.
        _locationPermissionState.value = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // NOTE this will throw a SecurityException if the permission is not granted
    @SuppressLint("MissingPermission")
    private fun _getLocationFlow(): Flow<Location> = callbackFlow {
        // Build the location request using the new builder API.
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000,
        )
            .setMinUpdateIntervalMillis(3000L) // Fastest rate for location updates.
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    trySend(location).isSuccess
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        // Clean up when the flow collection is cancelled.
        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}
