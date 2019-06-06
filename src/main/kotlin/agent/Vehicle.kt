package agent

import Dot
import DoubleVector
import check
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import world.WorldObject

class Vehicle(val body: Body, val sensors: Array<Sensor>, val motors: Array<Motor>, var speed: DoubleVector) {
    val render = VehicleRender(body, sensors, motors)

    /**
     * Changes velocity of current vehicle, based on sensors affected by objects in the world.
     */
    fun updateVelocity(affectors: Collection<WorldObject>) {
        var velVec: DoubleVector = this.speed
        affectors.forEach {
            val wo = it
            sensors.forEach { ite ->
                val shape = (ite.shape as Circle) //dirty
                velVec += wo.effectOnDistance(
                    shape.centerX, shape.centerY
                ) * ite.polarity
            }
        }
        this.speed = velVec
    }

    inner class VehicleRender(body: Body, sensors: Array<Sensor>, motors: Array<Motor>) : StackPane() {
        val list: MutableList<Shape> = mutableListOf()

        init {
            list.add(body.shape)
            motors.forEach { list.add(it.shape) }
            sensors.forEach { list.add(it.shape) }
        }

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
            val body =
                Body(
                    Rectangle(centerPositionX, centerPositionY, longSide, shortSide)
                )
            body.shape.fill = Color.MOCCASIN
            val bodyCenter = Dot(centerPositionX - shortSide / 2, centerPositionX + longSide / 2)
            val sensorRight = Sensor(
                Circle(centerPositionX + sensorsDistance / 2, centerPositionY + longSide / 2, sensorMotorRadius),
                polarity = 1
            )
            val sensorLeft = Sensor(
                Circle(centerPositionX - sensorsDistance / 2, centerPositionY + longSide / 2, sensorMotorRadius),
                polarity = -1
            )
            val motorRight = Motor(
                Circle(centerPositionX + sensorsDistance / 2, centerPositionY - longSide / 2, sensorMotorRadius)
            )
            val motorLeft = Motor(
                Circle(centerPositionX - sensorsDistance / 2, centerPositionY - longSide / 2, sensorMotorRadius)
            )

            return Vehicle(
                body,
                arrayOf(sensorLeft, sensorRight),
                arrayOf(motorLeft, motorRight),
                DoubleVector(speedX, speedY)
            )
        }
    }
}
