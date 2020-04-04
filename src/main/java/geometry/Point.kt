package geometry

import kotlin.math.*

data class Point(val latitude: Float, val longitude: Float)

/**
 * Calculate distance to another point on the Earth in meters.
 *
 * @param other the target point
 *
 * @return distance to that point in meters
 */
fun Point.distanceTo(other: Point): Int {
    fun haversine(x: Double) = sin(x / 2).pow(2)

    val earthRadius = 6371000

    val (thisLat, otherLat, deltaLat, deltaLong) = listOf(
        this.latitude,
        other.latitude,
        this.latitude - other.latitude,
        this.longitude - other.longitude
    ).map { Math.toRadians(it.toDouble()) }

    val a = haversine(deltaLat) + cos(thisLat) * cos(otherLat) * haversine(deltaLong)

    return round(earthRadius * 2 * atan2(sqrt(a), sqrt(1 - a))).toInt()
}

/**
 * Calculates angle between three points in degrees.
 *
 * @param a first point
 * @param b second point
 * @param c third point
 *
 * @return angle between points rounded to Int
 */
fun angle(a: Point, b: Point, c: Point): Int {
    val vectorA = a - b
    val vectorB = c - b

    return Math.toDegrees(
        acos((vectorA * vectorB) / (vectorA.vectorLength() * vectorB.vectorLength())).toDouble()
    ).roundToInt()
}

private fun Point.vectorLength(): Float = sqrt(latitude.pow(2) + longitude.pow(2))

private operator fun Point.times(other: Point): Float =
    latitude * other.latitude + longitude * other.longitude

private operator fun Point.minus(other: Point): Point =
    Point(latitude - other.latitude, longitude - other.longitude)

infix fun Float.toPoint(other: Float): Point = Point(this, other)


enum class Orientation {
    RIGHT,
    COLLINEAR,
    LEFT
}

fun orientation(a: Point, b: Point, c: Point): Orientation {
    val res = (c.latitude - a.latitude) * (b.longitude - a.longitude) -
        (c.longitude - a.longitude) * (b.latitude - a.latitude)

    return when {
        res < 0f -> Orientation.RIGHT
        res == 0f -> Orientation.COLLINEAR
        else -> Orientation.LEFT
    }
}

