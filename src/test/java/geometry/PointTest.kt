package geometry

import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

class PointTest : FreeSpec({
    "Point.distanceTo" - {
        "calculates correct distance" {
            forAll(
                row(59.966847f toPoint 30.305679f, 59.939640f toPoint 30.361640f, 4343),
                row(59.966847f toPoint 30.305679f, 59.942396f toPoint 30.273750f, 3248),
                row(59.966847f toPoint 30.305679f, 59.950147f toPoint 30.418632f, 6556),
                row(59.966847f toPoint 30.305679f, 59.935161f toPoint 30.448844f, 8715)
            ) { from, to, expectedDistance ->
                from.distanceTo(to) shouldBe expectedDistance
            }
        }
    }

    "Point.angle" - {
        "calculates correct angle with 3 deg error" - {
            val xs = listOf(2f, 2f, 1f, 1f, 0f)
            val ys = xs.reversed()
            val negXs = xs.map { -it }
            val negYs = ys.map { -it }
            val angles = listOf(
                0, 30, 45, 60, 90,
                180, 150, 135, 120, 90,
                180, 150, 135, 120, 90,
                0, 30, 45, 60, 90
            )

            val firstQuarter = xs.zip(ys).map { Point(it.first, it.second) }
            val secondQuarter = negXs.zip(ys).map { Point(it.first, it.second) }
            val thirdQuarter = negXs.zip(negYs).map { Point(it.first, it.second) }
            val fourthQuarter = xs.zip(negYs).map { Point(it.first, it.second) }

            (firstQuarter + secondQuarter + thirdQuarter + fourthQuarter).mapIndexed { i, point ->
                row("(${point.latitude}. ${point.longitude})", point, angles[i])
            }.map { (description, point, expectedAngle) ->
                description {
                    val actualAngle = angle(2f toPoint 0f, 0f toPoint 0f, point)
                    val range = (expectedAngle - 3 .. expectedAngle + 3)
                    (actualAngle in range) shouldBe true
                }
            }
        }
    }
})