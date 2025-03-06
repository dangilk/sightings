package com.djg.sightings.notification

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.djg.sightings.MainActivity
import com.djg.sightings.R
import com.djg.sightings.data.Alert
import com.djg.sightings.data.AlertRepository
import com.djg.sightings.data.SettingsKeys
import com.djg.sightings.ui.formatTimestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlertNotificationManager(
    private val context: Context,
    private val alertRepository: AlertRepository,
    private val ioScope: CoroutineScope,
    private val preferences: DataStore<Preferences>,
) {

    val channelId = "alerts_channel"
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alerts"
            val descriptionText = "Notifications for new alerts"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun launchChannelSettings(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
            }
            activity.startActivity(intent)
        }
    }

    fun isChannelEnabled(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel(channelId)
            // If channel is null, it hasn't been created yet
            return channel?.importance != NotificationManager.IMPORTANCE_NONE
        }
        // For devices below API 26, channels don't exist
        return true
    }

    fun handleAlert(
        alert: Alert,
    ) {
        // Save the alert to the database.
        ioScope.launch {
            alertRepository.insertAlert(alert)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification.
        val channelId = "alerts_channel"
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.material_symbols_outlined_flyover)
            .setContentTitle(alert.title)
            .setContentText("New Alert received at ${formatTimestamp(alert.date)}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Show the notification.
        with(NotificationManagerCompat.from(context)) {
            ioScope.launch {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    && preferences.data.first()
                        .toPreferences()[SettingsKeys.NOTIFICATIONS_ENABLED] == true
                ) {
                    notify(alert.id, notification)
                }
            }
        }
    }
}