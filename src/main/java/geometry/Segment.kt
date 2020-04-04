package geometry

import kotlin.math.*

data class Segment(val a: Point, val b: Point)

operator fun Segment.contains(point: Point): Boolean =
    orientation(a, b, point) == Orientation.COLLINEAR &&
        point.latitude in (min(a.latitude, b.latitude)..max(a.latitude, b.latitude))

fun Pair<Point, Point>.toSegment(): Segment = Segment(first, second)
