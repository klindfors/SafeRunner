package com.example.saferunner.data

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Duration
import java.time.Instant

data class Activity(
    val id: Long,
    val name: String,
    val startTime: Instant,
    val activityType: ActivityType,

    val distance: Double,
    val movingTime: Duration,
    val elapsedTime: Duration,
    val totalElevationGain: Double,

    val averageSpeed: Double,
    val maxSpeed: Double,

    val averageHeartRate: Double?,
    val maxHeartRate: Double?,
    val calories: Double?
) : Comparable<Activity> {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun compareTo(other: Activity): Int {
        val timeComparison = startTime.compareTo(other.startTime)

        return if (timeComparison != 0) {
            timeComparison
        } else {
            id.compareTo(other.id)
        }
    }
}