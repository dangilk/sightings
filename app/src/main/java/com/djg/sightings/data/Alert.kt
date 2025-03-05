package com.djg.sightings.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "alerts")
data class Alert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: Long,
    val latitude: Double,
    val longitude: Double,
    val isRead: Boolean
)

@Dao
interface AlertDao {
    companion object {
        const val EARTH_RADIUS_KM = 6371
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: Alert): Long

    suspend fun insertAlerts(alerts: List<Alert>) {
        alerts.forEach { insertAlert(it) }
    }

    @Update
    suspend fun updateAlert(alert: Alert)

    @Delete
    suspend fun deleteAlert(alert: Alert)

    @Query("SELECT * FROM alerts")
    suspend fun getAllAlerts(): List<Alert>

    @Query("SELECT * FROM alerts")
    fun getAlertsFlow(): Flow<List<Alert>>

    @Query(
        """
        SELECT * FROM alerts 
        WHERE latitude BETWEEN :minLat AND :maxLat 
          AND longitude BETWEEN :minLon AND :maxLon
        ORDER BY date DESC
    """
    )
    fun getAlertsInBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): Flow<List<Alert>>
}
