import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class SimModel(val world: SimWorld, var activeVehicles: Set<Vehicle>)

class SimWorld(val worldWidth: Double, val worldHeight: Double, val objects: Set<WorldObject>)

/**
 * effectStrength is relative to the vehicle's attraction
 */
class WorldObject(val x: Double, val y: Double, val effectStrength: Double) {

    /**
     * Newton's law. Returns (x, y) acceleration (mass is assumed to be 1 => a=F)
     */
    fun effectOnDistance(toX: Double, toY: Double): Array<Double> {
        val G = 10
        val rSquare = (toX - this.x).pow(2) + (toY - this.y).pow(2)
        val F = G * this.effectStrength / (rSquare)
        val alpha =
            computeAngle(arrayOf(Dot(x, y), Dot(toX, toY)), arrayOf(Dot(toX, toY), Dot(toX + 1, toY)), ::atan)
        return (arrayOf(cos(alpha), sin(alpha)))
    }
}