package geometry

sealed class Area {
    class Circle(private val center: Point, radius: Int) : Area() {
        private val radius: Int

        init {
            check(radius >= 0)
            this.radius = radius
        }

        override operator fun contains(point: Point): Boolean =
            if (radius == 0) center == point else radius >= center.distanceTo(point)
    }

    class Polygon(vertices: List<Point>) : Area() {
        private val vertices: List<Point>

        init {
            check(vertices.isNotEmpty())
            this.vertices = vertices
        }

        override operator fun contains(point: Point): Boolean {
            val edges = vertices.asSequence().zipWithNext() + (vertices.last() to vertices.first())
            var intersections = 0
            for (edge in edges) {
                if (point in edge.toSegment()) {
                    return true
                }

                val (a, b) = edge
                val (minPoint, maxPoint) =
                    if (a.longitude < b.longitude) {
                        a to b
                    } else {
                        b to a
                    }

                val edgeIsLower = maxPoint.longitude <= point.longitude
                val edgeIsUpper = minPoint.longitude > point.longitude
                if (edgeIsLower || edgeIsUpper) {
                    continue
                }

                when (orientation(minPoint, maxPoint, point)) {
                    Orientation.LEFT -> intersections++
                    Orientation.COLLINEAR -> return true
                    else -> Unit
                }
            }

            return intersections % 2 != 0
        }
    }

    abstract operator fun contains(point: Point): Boolean
}

fun polygonOf(point: Point, vararg points: Point): Area.Polygon =
    Area.Polygon(listOf(point) + points.toList())