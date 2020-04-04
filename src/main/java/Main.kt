import geometry.Area
import geometry.Point
import geometry.angle
import geometry.distanceTo
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.math.roundToInt

val START_POINT = Point(latitude = 59.9815845f, longitude = 30.2144768f)

data class Participants(val passengers: Collection<Person>, val drivers: Collection<Driver>)
open class Person(val id: UUID, val finishPoint: Point)

class Driver(id: UUID, finishPoint: Point, val preferredArea: Area?) : Person(id, finishPoint)


fun main() {
    val pathToResource = Paths.get(Participants::class.java.getResource("latlons").toURI())
    val (passengers, drivers) = readParticipants(pathToResource)

    for (passenger in passengers) {
        val suggestedDrivers = suggestDrivers(START_POINT, passenger, drivers)
        println("Passenger point: ${passenger.finishPoint.latitude}, ${passenger.finishPoint.longitude}")
        for (driver in suggestedDrivers) {
            println("  ${driver.finishPoint.latitude}, ${driver.finishPoint.longitude}")
        }
    }
}

/**
 * Suggest drivers to a passenger.
 *
 * Drivers are sorted in the following order:
 *   1. Passenger point lies in the area that driver prefers, if any
 *   2. Sort by "complexity" criteria described further
 *
 * The main idea of "complexity" criteria is how much is it harder
 * for driver to take passenger with them than to drive home alone. It is represented by coefficient:
 *     `(SP + PD) / SD`
 *       where
 *         SP is the distance from the start point to the passenger point
 *         PD is the distance from the passenger point to the driver point
 *         SD is the distance from the start point to the driver point
 * This coefficient equals 1 when all three points are on the same line and therefore there's
 * no additional actions from driver required to take passenger where needed.
 *
 * There are few cases though when this coefficient isn't really convenient:
 *   1. Taking two drivers, if their points are approximately at the same line with the passenger
 *      point, it will be more efficient if the driver that is closer picks up the passenger.
 *   2. If the passenger point is on the opposite side from the driver point, it's probably
 *      not very convenient for driver to go there and then return back especially if return
 *      distance is quite noticeable.
 * So there are additional checks for that cases.
 *
 * @param startPoint point where initially passenger and drivers are located
 * @param passenger passenger for a driver
 * @param drivers drivers willing to pick up a passenger
 * */
fun suggestDrivers(startPoint: Point, passenger: Person, drivers: Collection<Driver>): Collection<Person> =
    drivers.sortedWith(
        compareByDescending<Driver> { driver ->
            driver.preferredArea ?: return@compareByDescending true
            passenger.finishPoint in driver.preferredArea
        }.thenComparing { driver1: Driver, driver2: Driver ->
            val angle1 = angle(startPoint, passenger.finishPoint, driver1.finishPoint) / 10
            val angle2 = angle(startPoint, passenger.finishPoint, driver2.finishPoint) / 10

            if (angle1 == angle2) {
                startPoint.distanceTo(driver1.finishPoint)
                    .compareTo(startPoint.distanceTo(driver2.finishPoint))
            } else {
                compareBy<Driver> { driver ->
                    val angle = angle(startPoint, passenger.finishPoint, driver.finishPoint)
                    val startToPassenger = startPoint.distanceTo(passenger.finishPoint)
                    val passengerToDriver = passenger.finishPoint.distanceTo(driver.finishPoint)
                    val startToDriver = startPoint.distanceTo(driver.finishPoint)
                    val complexity = (startToPassenger + passengerToDriver) / startToDriver.toDouble()

                    if (angle < 40 && passengerToDriver > 0.7 * startToPassenger) {
                        startToPassenger + passengerToDriver
                    } else {
                        (complexity * 10).roundToInt()
                    }
                }.thenBy { driver ->
                    startPoint.distanceTo(driver.finishPoint)
                }.compare(driver1, driver2)
            }
        }
    )

/**
 * Read participants from the file given a path.
 *
 * File line should satisfy the following grammar:
 *     line = <comment> | <point> "|" <areaType> "|" <areaInfo>?
 *     comment = "#" .*
 *     areaType = "null" | "circle" | "polygon"
 *     areaInfo = <decimal as a radius of a circle> | <polygonPoints>
 *     polygonPoints = <polygonPoint> ("-" <polygonPoint>)*
 *     polygonPoint = "(" <point> ")"
 *     point = <latitude> ", " <longitude>
 *
 * Comments are ignored.
 *
 * @param pathToResource path to file with participants description
 *
 * @return [Participants] object from the parsed file
 */
fun readParticipants(pathToResource: Path): Participants {
    val allPoints = Files.readAllLines(pathToResource)
        .asSequence()
        .filterNot { it.startsWith("#") || it.isBlank() }
        .map { line ->
            val (sPoint, areaType, areaInfo) = line.split("|").map { it.trim() }
            val point = asPoint(sPoint)
            val area = when (areaType) {
                "null" -> null
                "circle" -> Area.Circle(point, areaInfo.toInt())
                "polygon" -> Area.Polygon(
                    areaInfo.split("-")
                        .map { it.trim() }
                        .map { asPoint(it.substring(1 until it.length)) }
                )
                else -> error("Unknown area type")
            }

            point to area
        }
        .toList()

    val passengers = allPoints.slice(0..9).map { Person(UUID.randomUUID(), it.first) }
    val drivers = allPoints.slice(10..20)
        .map { Driver(UUID.randomUUID(), it.first, it.second) }

    return Participants(passengers, drivers)
}

private fun asPoint(it: String): Point {
    val (lat, lon) = it.split(", ")
    return Point(lat.toFloat(), lon.toFloat())
}
