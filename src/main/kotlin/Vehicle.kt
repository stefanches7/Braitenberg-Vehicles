import javafx.scene.Node
import javafx.scene.layout.StackPane
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import tornadofx.*

class Vehicle(val body: Body, val sensors: Array<Sensor>, val motors: Array<Motor>, var speed: Vector) {
    val render = VehicleRender()

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

    inner class VehicleRender : StackPane() {
        val element: StackPane = stackpane {
            body.shape
            motors.forEach { it.shape }
            sensors.forEach { it.shape }
        }

        fun update(velocity: Vector) {
            val bodyparts: List<Node> = element.children.filter { it is Shape }
            bodyparts.forEach {
                it.layoutXProperty().animate(it.layoutX + velocity.x, 1.seconds)
                it.layoutYProperty().animate(it.layoutY + velocity.y, 1.seconds)
            }
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
