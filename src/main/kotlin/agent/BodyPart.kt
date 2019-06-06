package agent

import check
import javafx.scene.shape.Shape

interface BodyPart {
    val shape: Shape
}

class Body(
    override val shape: Shape
) : BodyPart

class Motor(override val shape: Shape) :
    BodyPart {

    fun move(processedSenses: FloatArray): FloatArray {
        return FloatArray(0)
    }
}

class Sensor(
    override val shape: Shape,
    val polarity: Int
) : BodyPart {

    init {
        check(polarity == 1 || polarity == -1) { throw IllegalArgumentException("Polarity must be 1 or -1!") }
    }

    fun feel(signal: FloatArray): FloatArray {
        return FloatArray(0)
    }
}