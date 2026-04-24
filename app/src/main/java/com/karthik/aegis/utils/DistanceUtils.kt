package com.karthik.aegis.utils

import kotlin.math.*

object DistanceUtils {
    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Haversine formula - calculates distance between two GPS coordinates
     * @return distance in meters
     */
    fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_METERS * c
    }

    /**
     * Calculate bearing between two points (in degrees)
     */
    fun bearing(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLng = Math.toRadians(lng2 - lng1)
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)

        val y = sin(dLng) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLng)

        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }

    /**
     * Check if a point is within a circle (geofence check)
     */
    fun isWithinRadius(centerLat: Double, centerLng: Double, pointLat: Double, pointLng: Double, radiusMeters: Double): Boolean {
        return distanceMeters(centerLat, centerLng, pointLat, pointLng) <= radiusMeters
    }

    /**
     * Calculate speed given two GPS points and time difference
     * @return speed in m/s
     */
    fun calculateSpeed(lat1: Double, lng1: Double, lat2: Double, lng2: Double, timeDiffMs: Long): Float {
        if (timeDiffMs <= 0) return 0f
        val distance = distanceMeters(lat1, lng1, lat2, lng2)
        return (distance / timeDiffMs * 1000).toFloat()
    }

    /**
     * Convert m/s to km/h
     */
    fun mpsToKmh(mps: Float): Float = mps * 3.6f

    /**
     * Convert km/h to m/s
     */
    fun kmhToMps(kmh: Float): Float = kmh / 3.6f

    /**
     * Format distance for display
     */
    fun formatDistance(meters: Double): String {
        return when {
            meters < 1000 -> "${meters.toInt()}m"
            else -> String.format("%.1fkm", meters / 1000)
        }
    }

    /**
     * Estimate arrival time based on distance and speed
     */
    fun estimateArrivalTime(distanceMeters: Double, speedMps: Float): Long {
        if (speedMps <= 0) return -1
        return (distanceMeters / speedMps).toLong()
    }
}