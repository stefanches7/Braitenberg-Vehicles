package agent

import Dot
import DoubleVector
import agent.brain.Network
import angleToXAxis
import check
import degrees
import javafx.animation.KeyValue
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import model.SimModel
import model.WorldObject
import sum
import tornadofx.*
import view.VehicleGroup
import kotlin.math.abs
import kotlin.random.Random

class Vehicle(
    val body: Body,
    val motors: Array<Motor>,
    val sensors: Array<Sensor>,
    var speed: DoubleVector,
    var brain: Network
) {
    val render = VehicleGroup(sensors.map { it.shape } + motors.map { it.shape } + listOf<Node>(body.shape))
    val model: SimModel by SimModel
    var oldSpeed: DoubleVector = DoubleVector(0.0, 0.0) //used for rotation computation

    fun getAngle() = angleToXAxis(Dot(this.speed.x, this.speed.y))
    fun getX() = this.body.shape.layoutX
    fun getY() = this.body.shape.layoutY


    /**
     * Changes velocity and dAngle of current vehicle, based on sensors affected by objects in the world.
     */
    fun updateSpeed(affectors: Collection<WorldObject>) {
        // save for angle computation
        this.oldSpeed = this.speed.copy()
        this.speed = this.perceptEffects(affectors)
    }

    private fun perceptEffects(affectors: Collection<WorldObject>): DoubleVector {
        val sensorInput = this.sensors.map { it.percept(affectors) }
        val motorOutput = this.brain.propagate(sensorInput.toTypedArray())
        val pureSpeed = motorOutput.sum()
        val adjustedSpeed = repulseFromWalls(pureSpeed)
        return adjustedSpeed
    }

    /**
     * Repulses vehicle off the wall, when it is close
     */
    private fun repulseFromWalls(speed: DoubleVector): DoubleVector {
        val (fromLeft, fromUp) = arrayOf(abs(this.getX()), abs(this.getY()))
        if (fromLeft == 0.0 || fromUp == 0.0) return speed//just initialized
        val (fromRight, fromDown) = arrayOf(abs(model.worldEnd.x - fromLeft), abs(model.worldEnd.y - fromUp))
        // truncate speed vectors to out of bounds
        val out = adjustSpeedInLimits(speed, arrayOf(fromLeft, fromUp, fromRight, fromDown))
        val c = 100.0
        val adjustedSpeed = DoubleVector(
            out.x + repulseFun(fromLeft, c) - repulseFun(fromRight, c),
            out.y + repulseFun(fromUp, c) - repulseFun(fromDown, c)
        )
        return adjustedSpeed
    }

    private fun adjustSpeedInLimits(speed: DoubleVector, distances: Array<Double>): DoubleVector {
        val out = speed
        val (fromLeft, fromUp, fromRight, fromDown) = distances
        if (fromLeft + speed.x > model.worldEnd.x) out.x = (model.worldEnd.x - fromLeft) * 0.9
        else if (fromRight + speed.x < 0) out.x = (fromRight - 0) * 0.9
        if (fromUp + speed.y > model.worldEnd.y) out.y = (model.worldEnd.y - fromUp) * 0.9
        else if (fromDown + speed.y < 0) out.x = (fromDown - 0) * 0.9
        return out
    }

    /**
     * c is "repulse closer than points" parameter
     */
    fun repulseFun(distance: Double, c: Double): Double {
        if (abs(distance) > c) return 0.0
        else return abs(1 / abs(distance / c))
    }


    /**
     * Next rotation angle, given in radians.
     */
    fun rotationAngle(): Double {
        // Delta of the angles of old and current speed vector
        val angleNow = this.getAngle()
        val anglePrev = angleToXAxis(
            Dot(this.oldSpeed.x, this.oldSpeed.y)
        )
        return angleNow - anglePrev
    }


    /**
     * Takes speed and dAngle now and calculates transformations for each body part
     */
    fun calcCurrentUpdate(affectors: MutableSet<WorldObject>): Set<KeyValue> {
        this.updateSpeed(affectors)
        val animation = animationChanges()
        return animation
    }

    fun animationChanges(): Set<KeyValue> {
        val out: MutableSet<KeyValue> = mutableSetOf()
        out.add(this.bodyRotation())
        out.addAll(this.moveBodyParts())
        out.addAll(this.moveBody())
        return out
    }

    private fun moveBody(): Collection<KeyValue> {
        val out: MutableSet<KeyValue> = mutableSetOf()
        out.add(KeyValue(body.shape.layoutXProperty(), this.getX() + this.speed.x))
        out.add(KeyValue(body.shape.layoutYProperty(), this.getY() + this.speed.y))
        return out
    }

    fun bodyRotation(): KeyValue {
        val rotateAngle = this.rotationAngle()
        return KeyValue(body.shape.rotateProperty(), body.shape.rotate + rotateAngle.degrees())
    }

    fun moveBodyParts(): Set<KeyValue> {
        val out: MutableSet<KeyValue> = mutableSetOf()
        val transform = { bp: BodyPart ->
            val oldBPOffset = bp.centerOffset.copy()
            val rotAngle = this.rotationAngle()
            bp.rotateAroundCenter(rotAngle.rad)
            // bp center offset is already angle updated
            val newX = bp.getX() - oldBPOffset.x + bp.centerOffset.x + this.speed.x
            val newY = bp.getY() - oldBPOffset.y + bp.centerOffset.y + this.speed.y
            out.add(KeyValue(bp.getXProperty(), newX))
            out.add(KeyValue(bp.getYProperty(), newY))
        }
        this.sensors.forEach {
            transform(it)
        }
        this.motors.forEach {
            transform(it)
        }
        return out
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
            speedY: Double,
            brain: Network? = null,
            brainSize: Int = 5
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
                polarity = 1
            )
            val motorRight = Motor(
                centerOffset = DoubleVector(longSide / 2, -sensorsDistance / 2),
                bodyCenter = bodyCenter
            )
            val motorLeft = Motor(
                centerOffset = DoubleVector(longSide / 2, sensorsDistance / 2),
                bodyCenter = bodyCenter
            )
            var br = brain
            if (br == null)
                br = Network.generateRandomOfSize(brainSize)

            return Vehicle(
                body,
                arrayOf(motorLeft, motorRight),
                arrayOf(sensorLeft, sensorRight),
                DoubleVector(speedX, speedY),
                br
            )
        }

        fun randomSimpleVehicle(
            worldWidth: Double, worldHeight: Double,
            vehicleLength: Double = Math.floor(worldWidth / 80),
            vehicleHeight: Double = Math.floor(worldHeight / 150),
            sensorsDistance: Double = vehicleHeight / 2.0,
            brain: Network? = null
        ): Vehicle {
            return Vehicle.Factory.simpleVehicle(
                Random.nextDouble(0.0, worldWidth),
                Random.nextDouble(0.0, worldHeight),
                vehicleHeight, vehicleLength, 1.0, sensorsDistance,
                Random.nextDouble(-10.0, 10.0),
                Random.nextDouble(-10.0, 10.0),
                brain = brain
            )

        }
    }
}
