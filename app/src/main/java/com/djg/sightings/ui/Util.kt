package com.djg.sightings.ui

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@SuppressLint("DefaultLocale")
fun formatLatLon(lat: Double, lon: Double): String {
    return "Lat: ${String.format("%.4f", lat)}, Lon: ${String.format("%.4f", lon)}"
}