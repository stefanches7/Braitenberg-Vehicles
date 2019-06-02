import javafx.scene.shape.Circle
import javafx.scene.shape.Shape
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

class SimModel(val world: SimWorld, var activeVehicles: Set<Vehicle>)

class SimWorld(val worldWidth: Double, val worldHeight: Double, val objects: MutableSet<WorldObject>)

/**
 * effectStrength is relative to the vehicle's attraction
 */
class WorldObject(val x: Double, val y: Double, val size: Double, val effectStrength: Double) {
    val shape: Shape = Circle(x, y, size)

    /**
     * Newton's law. Returns (x, y) acceleration (mass is assumed to be 1 => a=F)
     */
    fun effectOnDistance(toX: Double, toY: Double): Vector {
        val G = 10
        val rSquare = (toX - this.x).pow(2) + (toY - this.y).pow(2)
        val F = G * this.effectStrength / (rSquare)
        val alpha =
            computeAngle(arrayOf(Dot(x, y), Dot(toX, toY)), arrayOf(Dot(toX, toY), Dot(toX + 1, toY)), ::atan)
        return Vector(F * cos(alpha), F * sin(alpha))
    }

    companion object {
        fun randomWorldObject(worldWidth: Double, worldHeight: Double): WorldObject {
            return WorldObject(
                Random.nextDouble(worldWidth),
                Random.nextDouble(worldHeight),
                Random.nextDouble(1.0, 10.0),
                Random.nextDouble(1.0, 10.0)
            )
        }
    }
}