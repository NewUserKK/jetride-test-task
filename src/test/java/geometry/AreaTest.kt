package geometry

import geometry.Area.*
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class AreaTest : FreeSpec({
    "Area.Circle" - {
        "constructor" - {
            "should succeed when radius is not negative" {
                shouldNotThrowAny {
                    Circle(0f toPoint 0f, 0)
                    Circle(0f toPoint 0f, 1)
                }
            }

            "should fail when radius is negative" {
                shouldThrowAny {
                    Circle(0f toPoint 0f, -1)
                }
            }
        }

        "contains" - {
            "should return true if point lies in this area" {
                forAll(
                    row(59.966847f toPoint 30.305679f, 5000, 59.939640f toPoint 30.361640f),
                    row(59.966847f toPoint 30.305679f, 4000, 59.942396f toPoint 30.273750f),
                    row(59.966847f toPoint 30.305679f, 7000, 59.950147f toPoint 30.418632f)
                ) { center, radius, point ->
                    Circle(center, radius).contains(point) shouldBe true
                }
            }

            "should return true if point lies on circle border" {
                val area = Circle(59.966847f toPoint 30.305679f, 4343)
                val point = 59.939640f toPoint 30.361640f
                (point in area) shouldBe true
            }

            "point lies in area with zero radius only if it is its center" {
                val area = Circle(59.966847f toPoint 30.305679f, 0)
                area.contains(59.966847f toPoint 30.305679f) shouldBe true
                area.contains(59.966847f toPoint 30.305678f) shouldBe false
            }

            "should return false if point is outside of area" {
                forAll(
                    row(59.966847f toPoint 30.305679f, 3000, 59.939640f toPoint 30.361640f),
                    row(59.966847f toPoint 30.305679f, 3000, 59.942396f toPoint 30.273750f),
                    row(59.966847f toPoint 30.305679f, 3000, 59.950147f toPoint 30.418632f)
                ) { center, radius, point ->
                    Circle(center, radius).contains(point) shouldBe false
                }
            }
        }
    }

    "Area.Polygon" - {
        "constructor" - {
            "should succeed if vertices list is not empty" {
                shouldNotThrowAny {
                    Polygon(nPoints(3))
                    Polygon(nPoints(2))
                    Polygon(nPoints(1))
                }
            }
            "should fail if vertices list is empty" {
                shouldThrowAny {
                    Polygon(listOf())
                }
            }
        }

        "contains" - {
            val testPolygon = polygonOf(
                60.00001f toPoint 60.00001f,
                60.00000f toPoint 60.00003f,
                60.00001f toPoint 60.00005f,
                60.00004f toPoint 60.00004f,
                60.00003f toPoint 60.00003f,
                60.00003f toPoint 60.00002f,
                60.00004f toPoint 60.00002f,
                60.00003f toPoint 60.00001f,
                60.00002f toPoint 60.000005f
            )

            "should return true if point lies in this area" {
                forAll(
                    row(60.00001f toPoint 60.00003f),
                    row(60.00002f toPoint 60.00002f),
                    row(60.000025f toPoint 60.00003f),
                    row(60.00003f toPoint 60.0000375f)
                ) { point ->
                    (point in testPolygon) shouldBe true
                }
            }

            "should return true if point lies on polygon border" {
                forAll(
                    row(60.00001f toPoint 60.00005f),
                    row(60.000035f toPoint 60.00002f),
                    row(60.00003f toPoint 60.000025f),
                    row(60.00002f toPoint 60.000005f)
                ) { point ->
                    (point in testPolygon) shouldBe true
                }
            }

            "should return false if point is outside of area" {
                forAll(
                    row(60.00001f toPoint 60.00006f),
                    row(60.000005f toPoint 60.00001f),
                    row(60.000035f toPoint 60.00003f),
                    row(60.00002f toPoint 60.00005f)
                ) { point ->
                    (point in testPolygon) shouldBe false
                }
            }
        }
    }
})

private fun nPoints(n: Int): List<Point> =
    generateSequence { 0f toPoint 0f }.take(n).toList()