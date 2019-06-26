package agent.brain

import java.util.*

/**
 * Binary representation of various data structures.
 */
class BinaryRepresentation(val representation: BitSet) {

    operator fun get(from: Int = 0, to: Int = representation.size()): BinaryRepresentation {
        return BinaryRepresentation(representation[from, to])
    }

    operator fun plus(other: BinaryRepresentation): BinaryRepresentation {
        return BinaryRepresentation(BitSet.valueOf(this.representation.toByteArray() + other.representation.toByteArray()))
    }

    fun flip(pos: Int) {
        check(pos < this.length()) { println("Trying to flip bit out of bounds!") }
        this.representation.flip(pos)
    }

    fun length(): Int {
        return representation.size()
    }

    fun toByteArray(): ByteArray {
        return representation.toByteArray()!!
    }

    companion object {

        fun valueOf(arr: ByteArray): BinaryRepresentation {
            return BinaryRepresentation(BitSet.valueOf(arr))
        }

    }

}