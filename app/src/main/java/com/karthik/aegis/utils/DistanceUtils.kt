package com.karthik.aegis.utils

import kotlin.math.*

object DistanceUtils {

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    /**
     * Calculate distance between two lat/lng points using Haversine formula
     * @return distance in meters
     */
    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val lat1Rad = lat1.toRad()
        val lat2Rad = lat2.toRad()
        val deltaLat = (lat2 - lat1).toRad()
        val deltaLon = (lon2 - lon1).toRad()

        val a = sin(deltaLat / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)

        val c = 2 * asin(sqrt(a))

        return EARTH_RADIUS_METERS * c
    }

    /**
     * Check if point is inside circular zone
     */
    fun isInsideZone(
        centerLat: Double,
        centerLon: Double,
        radiusMeters: Double,
        pointLat: Double,
        pointLon: Double
    ): Boolean {
        val distance = distanceMeters(centerLat, centerLon, pointLat, pointLon)
        return distance <= radiusMeters
    }

    /**
     * Get bearing between two points (in degrees, 0-360)
     */
    fun bearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val lat1Rad = lat1.toRad()
        val lat2Rad = lat2.toRad()
        val deltaLon = (lon2 - lon1).toRad()

        val x = sin(deltaLon) * cos(lat2Rad)
        val y = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(deltaLon)

        val bearingRad = atan2(x, y)
        val bearingDeg = bearingRad.toDegrees()

        return (bearingDeg + 360) % 360
    }

    /**
     * Calculate speed in m/s from distance moved and time elapsed
     */
    fun speed(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        timeElapsedMs: Long
    ): Float {
        if (timeElapsedMs <= 0) return 0f
        val distanceMeters = distanceMeters(lat1, lon1, lat2, lon2)
        val timeSeconds = timeElapsedMs / 1000.0
        return (distanceMeters / timeSeconds).toFloat()
    }

    private fun Double.toRad(): Double = this * Math.PI / 180.0
}
