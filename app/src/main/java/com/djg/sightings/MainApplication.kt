package com.djg.sightings

import android.app.Application
import com.djg.sightings.data.AlertRepository
import com.djg.sightings.data.MainDatabase
import com.djg.sightings.data.MockData
import com.djg.sightings.data.settingsStore
import com.djg.sightings.notification.AlertNotificationManager
import com.djg.sightings.service.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainApplication : Application() {
    // do "manual DI" here. In a real app, I'd use Hilt or similar
    // - Although on a small app that may be overkill anyway
    val ioScope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
    private val db by lazy { MainDatabase.getDatabase(this) }
    val alertRepository by lazy { AlertRepository(alertDao = db.alertDao()) }
    val locationService by lazy { LocationService(this) }
    val alertNotificationManager by lazy {
        AlertNotificationManager(
            context = this,
            alertRepository = alertRepository,
            ioScope = ioScope,
            preferences = this.settingsStore
        )
    }
    val mockData by lazy {
        MockData(
            locationService = locationService,
            alertNotificationManager = alertNotificationManager
        )
    }
    // end DI stuff

    override fun onCreate() {
        super.onCreate()
        alertNotificationManager.createNotificationChannel()
    }
}