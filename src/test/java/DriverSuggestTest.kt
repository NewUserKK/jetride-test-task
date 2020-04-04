import io.kotest.core.spec.style.FreeSpec
import java.nio.file.Paths

class DriverSuggestTest : FreeSpec({

    fun readInput(): Participants {
        val pathToResource = Paths.get(this::class.java.getResource("latlons").toURI())
        return readParticipants(pathToResource)
    }

    /*
     * The results of this test are situated in "example" directory
     */
    "print all" {
        val (passengers, drivers) = readInput()

        for (passenger in passengers) {
            val suggestedDrivers = suggestDrivers(START_POINT, passenger, drivers)
            println("Passenger point: ${passenger.finishPoint.latitude}, ${passenger.finishPoint.longitude}")
            for (driver in suggestedDrivers) {
                println("  ${driver.finishPoint.latitude}, ${driver.finishPoint.longitude}")
            }
        }
    }
})