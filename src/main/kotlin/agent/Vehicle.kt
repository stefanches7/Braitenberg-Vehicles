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
import model.SimModel
import rotate
import sum
import world.WorldObject
import kotlin.math.abs
import kotlin.math.log10

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
    var x = this.body.shape.layoutX
        get() = this.body.shape.layoutX
    var y = this.body.shape.layoutX
        get() = this.body.shape.layoutX
//    var center = Dot(body.shape.layoutX + body.centerOffset.x, body.shape.layoutY + body.centerOffset.y)
//        get() = Dot(body.shape.layoutX + body.centerOffset.x, body.shape.layoutY + body.centerOffset.y)

    /**
     * Changes velocity and dAngle of current vehicle, based on sensors affected by objects in the world.
     */
    fun updateMovementVector(affectors: Collection<WorldObject>) {
        // save for angle computation
        this.oldSpeed = this.speed.copy()
        sensors.forEach {
            this.speed += it.feel(affectors)
        }

    }

    /**
     * Repulses vehicle off the wall, when it is close
     */
    private fun wallsRepulsion() {
        val aspiredX = this.x + this.speed.x
        val aspiredY = this.y + this.speed.y

        val (toLeft, toUp) = arrayOf(this.x, this.y)
        val (toRight, toDown) = arrayOf(abs(SimModel.worldEnd.x - toLeft), abs(SimModel.worldEnd.y - toUp))
        // truncate speed vectors to out of bounds
        if (aspiredX > SimModel.worldEnd.x) this.speed.x = (SimModel.worldEnd.x - this.x) * 0.9
        if (aspiredY > SimModel.worldEnd.y) this.speed.y = (SimModel.worldEnd.y - this.y) * 0.9
        this.speed.x += abs(log10(toLeft / toRight)) //log a + log b = log ab
        this.speed.y += abs(log10(toUp / toDown))
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


    /**
     * Takes speed and dAngle now and calculates transformations for each body part
     */
    fun currentUpdate(objects: MutableSet<WorldObject>): Set<KeyValue> {
        this.updateMovementVector(objects)
        val out: MutableSet<KeyValue> = mutableSetOf()
        // rotate body
        val rotateAngle = this.rotateAngle()
        out.add(KeyValue(body.shape.rotateProperty(), body.shape.rotate + rotateAngle.degrees()))
        this.body.centerOffset.rotate(rotateAngle)
        // move parts of the body
        val put = { bp: BodyPart ->
            val shape: Circle = bp.shape as Circle //TODO generalize cast
            bp.rotateAroundCenter(rotateAngle)
            // center offset is already angle updated
            val shiftX = sum(this.speed.x, bp.centerOffset.x + 1.0) //+radius
            val shiftY = sum(this.speed.y, bp.centerOffset.y + 1.0)
            out.add(KeyValue(shape.translateXProperty(), shiftX))
            out.add(KeyValue(shape.translateYProperty(), shiftY))
        }
        this.sensors.forEach {
            put(it)
        }
        this.motors.forEach {
            put(it)
        }
        // move body
        out.add(KeyValue(body.shape.translateXProperty(), this.speed.x))
        out.add(KeyValue(body.shape.translateYProperty(), this.speed.y))
        return out
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
