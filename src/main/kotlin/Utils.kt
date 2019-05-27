/**
 * This class is more for trying around, than bringing real value.
 */

tailrec fun fac(to: Int): Int {
    return (if (to == 1) 1 else fac(to - 1))
}

/**
 * Validation for boolean predicates.
 */
inline fun check(value: Boolean, lazyMessage: () -> Any): Unit {
    if (!value) {
        val message = lazyMessage()
        throw IllegalStateException(message.toString())
    }
}

fun computeAngle(firstLineTwoDots: Array<Dot>, secondLineTwoDots: Array<Dot>, atan: (Double) -> Double): Double {
    check(firstLineTwoDots.size >= 2 && secondLineTwoDots.size >= 2) { throw IllegalArgumentException("Not enough coordinates!") }
    val (l1d1, l1d2) = firstLineTwoDots
    val (l2d1, l2d2) = secondLineTwoDots
    val slL1 = (l1d2.y - l1d1.y) / (l1d2.x - l1d1.x)
    val slL2 = (l2d2.y - l2d1.y) / (l2d2.x - l2d1.x)
    val tan = slL1 * slL2 / (1 + slL1 * slL2)
    return atan(tan)
}

data class Dot(val x: Double, val y: Double)

class Vector(val x: Double, val y: Double) {
    operator fun plus(v: Vector): Vector {
        return Vector(this.x + v.x, this.y + v.y)

    }

    operator fun plus(d: DoubleArray): Vector {
        return Vector(this.x + d[0], this.y + d[1])
    }

    operator fun unaryMinus() = Vector(-x, -y)

    operator fun times(v: Vector) = Vector(x * v.x, y * v.y)

    operator fun times(c: Double) = Vector(c * x, c * y)
    operator fun times(c: Int) = Vector(c * x, c * y)
}