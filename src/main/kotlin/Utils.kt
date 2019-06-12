import javafx.geometry.Bounds
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

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

// TODO FIX wrong angle calculation!
fun angleToXAxis(l1d1: Dot, l1d2: Dot = Dot(0.0, 0.0)): Double {
    val alpha = atan2((l1d2.y - l1d1.y), (l1d2.x - l1d1.x)) //radian = slope
    return (if (alpha > 0.0) alpha else alpha + 2 * PI)
}

data class Dot(val x: Double, val y: Double)

data class DoubleVector(var x: Double, var y: Double) {
    operator fun plus(v: DoubleVector): DoubleVector {
        this.x += v.x
        this.y += v.y
        return this
    }

    operator fun plus(d: DoubleArray) = run {
        this.x += d[0]
        this.y += d[1]
        this
    }

    operator fun unaryMinus() = run {
        this.x = -x
        this.y = -y
        this
    }

    operator fun times(v: DoubleVector) = DoubleVector(x * v.x, y * v.y)

    operator fun times(c: Double) = DoubleVector(c * x, c * y)
    operator fun times(c: Int) = DoubleVector(c * x, c * y)
}

fun center(bounds: Bounds): Dot {
    return Dot((bounds.minX + bounds.maxX) / 2, (bounds.minY + bounds.maxY) / 2)
}

fun sum(vararg elements: Double): Double {
    var out = 0.0
    elements.forEach { out += it }
    return out
}

fun prod(vararg elements: Double): Double {
    var out = 0.0
    elements.forEach { out *= it }
    return out
}

fun DoubleVector.rotate(theta: Double) {
    // angle transformation multiplication
    val x_tick = this.x * cos(theta) - this.y * sin(theta)
    val y_tick = this.y * cos(theta) + this.x * sin(theta)
    this.x = x_tick
    this.y = y_tick
}

/**
 * Radian to degrees conversion
 */
fun Double.degrees(): Double {
    return this * 180 / PI
}