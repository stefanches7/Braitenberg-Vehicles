package agent

import check
import javafx.scene.shape.Shape

interface BodyPart {
    val shape: Shape
    val centerOffX: Double
    val centerOffY: Double
}

class Body(
    override val shape: Shape,
    override val centerOffX: Double,
    override val centerOffY: Double
) : BodyPart

class Motor(override val shape: Shape, override val centerOffX: Double, override val centerOffY: Double) :
    BodyPart {

    fun move(processedSenses: FloatArray): FloatArray {
        return FloatArray(0)
    }
}

class Sensor(
    override val shape: Shape,
    override val centerOffX: Double, //
    override val centerOffY: Double,
    val polarity: Int
) : BodyPart {
    init {
        check(polarity == 1 || polarity == -1) { throw IllegalArgumentException("Polarity must be 1 or -1!") }
    }

    fun feel(signal: FloatArray): FloatArray {
        return FloatArray(0)
    }
}