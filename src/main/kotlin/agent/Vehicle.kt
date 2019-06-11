package agent

import Dot
import DoubleVector
import angleToXAxis
import check
import degrees
import javafx.animation.KeyValue
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import rotate
import sum
import world.WorldObject

class Vehicle(
    val body: Body,
    val motors: Array<Motor>,
    val sensors: Array<Sensor>,
    var speed: DoubleVector
) {
    val render = VehicleRender(body, sensors, motors)
    var oldSpeed: DoubleVector = DoubleVector(0.0, 0.0) //used for rotation computation
    var angle = 0.0
        get() = angleToXAxis(Dot(this.speed.x, this.speed.y))
    var center = Dot(body.shape.layoutX + body.centerOffset.x, body.shape.layoutY + body.centerOffset.y)
        get() = Dot(body.shape.layoutX + body.centerOffset.x, body.shape.layoutY + body.centerOffset.y)

    /**
     * Changes velocity and dAngle of current vehicle, based on sensors affected by objects in the world.
     */
    fun updateMovementVector(affectors: Collection<WorldObject>) {
        var velVec: DoubleVector = this.speed.copy()
        oldSpeed = this.speed.copy()
        affectors.forEach {
            val wo = it
            sensors.forEach { ite ->
                velVec += wo.effectOnDistance(
                    ite.x, ite.y
                ) * ite.polarity
            }
        }
        this.speed = velVec
    }

    /**
     * Next rotation angle, given in radians.
     */
    fun rotateAngle(): Double {
        // Delta of the angles of old and current speed vector
        return this.angle - angleToXAxis(
            Dot(this.oldSpeed.x, this.oldSpeed.y)
        )
    }


    inner class VehicleRender(body: Body, sensors: Array<Sensor>, motors: Array<Motor>) : StackPane() {
        val list: MutableList<Shape> = mutableListOf()

        init {
            list.add(body.shape)
            motors.forEach { list.add(it.shape) }
            sensors.forEach { list.add(it.shape) }
        }

        /**
         * Takes speed and dAngle now and calculates transformations for each body part
         */
        fun currentUpdate(): Set<KeyValue> {
            val out: MutableSet<KeyValue> = mutableSetOf()
            // rotate body
            out.add(KeyValue(body.shape.rotateProperty(), body.shape.rotate + rotateAngle().degrees()))
            body.centerOffset.rotate(-rotateAngle())
            // move parts of the body
            val put = { bp: BodyPart ->
                val shape: Circle = bp.shape as Circle //TODO generalize cast
                //bp.rotateAroundCenter(rotateAngle())
                // center offset is already angle updated
                val shiftX = sum(speed.x, bp.centerOffset.x + 1.0) //+radius
                val shiftY = sum(speed.y, bp.centerOffset.y + 1.0)
                out.add(KeyValue(shape.translateXProperty(), shiftX))
                out.add(KeyValue(shape.translateYProperty(), shiftY))
            }
            sensors.forEach {
                put(it)
            }
            motors.forEach {
                put(it)
            }
            // move body
            out.add(KeyValue(body.shape.translateXProperty(), speed.x))
            out.add(KeyValue(body.shape.translateYProperty(), speed.y))
            return out
        }

    }

    companion object Factory {
        /**
         * Rectangular, round sensors, round motors, straight sensors-motors of different polarities.
         */
        fun simpleVehicle(
            leftTopX: Double,
            leftTopY: Double,
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
                    Rectangle(leftTopX, leftTopY, longSide, shortSide),
                    DoubleVector(longSide / 2, shortSide / 2)
                )
            body.shape.fill = Color.MOCCASIN
            // Rectangle is default positioned with long side horisontally
            val bodyCenter = Dot(leftTopX + longSide / 2, leftTopY + shortSide / 2)
            val sensorRight = Sensor(
                centerOffset = DoubleVector(-longSide / 2, -sensorsDistance / 2),
                bodyCenter = bodyCenter,
                polarity = 1
            )
            val sensorLeft = Sensor(
                centerOffset = DoubleVector(-longSide / 2, sensorsDistance / 2),
                bodyCenter = bodyCenter,
                polarity = -1
            )
            val motorRight = Motor(
                centerOffset = DoubleVector(longSide / 2, -sensorsDistance / 2),
                bodyCenter = bodyCenter
            )
            val motorLeft = Motor(
                centerOffset = DoubleVector(longSide / 2, sensorsDistance / 2),
                bodyCenter = bodyCenter
            )

            return Vehicle(
                body,
                arrayOf(motorLeft, motorRight),
                arrayOf(sensorLeft, sensorRight),
                DoubleVector(speedX, speedY)
            )


        }
    }
}
