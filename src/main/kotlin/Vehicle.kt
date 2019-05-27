import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle

class Vehicle(val body: Body, val sensors: Array<Sensor>, val motors: Array<Motor>, var speed: Vector) {

    fun updateVelocity(affectors: Collection<WorldObject>) {
        var velVec: Vector = this.speed
        affectors.forEach {
            val wo = it
            sensors.forEach { ite ->
                velVec += wo.effectOnDistance(ite.shape.layoutX, ite.shape.layoutY) * ite.polarity
            }
        }
        this.speed = velVec
    }

    companion object Factory {
        /**
         * Rectangular, round sensors, round motors, straight sensors-motors of different polarities.
         */
        fun simpleVehicle(
            centerPositionX: Double,
            centerPositionY: Double,
            shortSide: Double,
            longSide: Double,
            sensorMotorRadius: Double,
            sensorsDistance: Double,
            speedX: Double,
            speedY: Double
        ): Vehicle {
            check(sensorsDistance <= shortSide) {
                throw IllegalArgumentException("Sensors distance should be shorter than side!")
            }
            val body = Body(Rectangle(shortSide, longSide), centerOffX = centerPositionX, centerOffY = centerPositionY)
            val sensorRight = Sensor(
                Circle(centerPositionX + sensorsDistance / 2, centerPositionY + longSide / 2, sensorMotorRadius),
                centerOffY = longSide / 2,
                centerOffX = sensorsDistance / 2,
                polarity = 1
            )
            val sensorLeft = Sensor(
                Circle(centerPositionX - sensorsDistance / 2, centerPositionY + longSide / 2, sensorMotorRadius),
                centerOffY = longSide / 2,
                centerOffX = -sensorsDistance / 2,
                polarity = -1
            )
            val motorRight = Motor(
                Circle(centerPositionX + sensorsDistance / 2, centerPositionY - longSide / 2, sensorMotorRadius),
                centerOffY = -longSide / 2,
                centerOffX = sensorsDistance / 2
            )
            val motorLeft = Motor(
                Circle(centerPositionX - sensorsDistance / 2, centerPositionY - longSide / 2, sensorMotorRadius),
                centerOffY = -longSide / 2,
                centerOffX = -sensorsDistance / 2
            )

            return Vehicle(
                body,
                arrayOf(sensorLeft, sensorRight),
                arrayOf(motorLeft, motorRight),
                Vector(speedX, speedY)
            )
        }
    }
}
