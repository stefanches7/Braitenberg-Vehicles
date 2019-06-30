package agent.brain

import flip
import pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Binary representation of various data structures.
 */
@ExperimentalUnsignedTypes
class BinaryRepresentation(val representation: UByteArray) {

    operator fun get(from: Int, to: Int): UByteArray {
        return (representation.sliceArray(IntRange(from, to)))
    }

    operator fun get(at: Int): UByte {
        return representation[at]
    }

    operator fun plus(other: BinaryRepresentation): BinaryRepresentation {
        return BinaryRepresentation(this.representation + other.representation)
    }

    fun flip(pos: Int) {
        check(pos < this.length()) { println("Trying to flip bit out of bounds!") }
        val which = pos / 8
        this.representation[which] = this.representation[which].flip(pos % 8)
    }

    fun crossover(other: BinaryRepresentation, pos: Int = Random.nextInt(this.length() * 8)): BinaryRepresentation {
        check(other.length() == this.length()) { throw Exception("Can not crossover two different-length strings!") }
        check(pos < this.length()) { throw Exception("Trying to crossover out of bounds!") }
        val e1 = this[pos / 8]
        val e2 = other[pos / 8]
        val oursFirstCP: UByte = (e1.div(2.pow(7 - pos % 8).toUByte()) + e2.rem(2.pow(7 - pos % 8).toUByte())).toUByte()
        val theirFirstCP: UByte =
            (e2.div(2.pow(7 - pos % 8).toUByte()) + e1.rem(2.pow(7 - pos % 8).toUByte())).toUByte()
        val oursFirst: UByteArray = (this[0, pos / 8 - 1] + oursFirstCP) + other[pos / 8 + 1, this.length()]
        val theirFirst: UByteArray = other[0, pos / 8 - 1] + theirFirstCP + this[pos / 8 + 1, this.length()]
        val childrenVariants = arrayOf(
            oursFirst, theirFirst
        )
        return BinaryRepresentation(childrenVariants[Random.nextInt(childrenVariants.size)])
    }

    fun length(): Int {
        return representation.size
    }

    fun toByteArray(): UByteArray {
        return representation.toUByteArray()
    }

    /**
     * Tells how many nodes there are in network based on
     */
    fun nodesSize(): Int {
        val chunksCount = this.length()
        val nodesCount =
            (0.5 + sqrt((1 + 8 * chunksCount).toDouble()) / 2).roundToInt() //2nd degree polynomial n^2 - n - 2c = 0 solution
        return nodesCount
    }

}
