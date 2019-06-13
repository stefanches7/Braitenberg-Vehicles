package agent

import DoubleVector
import java.util.*

open class Network(
    val innerNeurons: Set<Neuron>,
    val inputNeurons: Array<Neuron>,
    val outputNeurons: Array<Neuron>
) {

    /**
     * Propagate the signal and get the vector output for motors
     */
    fun propagate(signal: Array<DoubleVector>): Array<DoubleVector> {
        return arrayOf()
    }

    /**
     * Binary representation of network.
     */
    fun toBinary(): BitSet {
        return BitSet()
    }

    companion object Factory {

        /**
         * 1 hidden layer, 2*2d input and output.
         */
        fun smallAutoencoder(): Network {
            val (i1, i2) = arrayOf(
                NormalizedInputNeuron(arrayOf(DoubleVector(doubleArrayOf(1.0, 1.0)))),
                NormalizedInputNeuron(arrayOf(DoubleVector(doubleArrayOf(1.0, 1.0)))),
                )
            val h1 = NormalizedInnerNeuron(
                setOf(Pair(i1, 0), Pair(i2, 0)),
                arrayOf(DoubleVector(doubleArrayOf(0.5, 0.5)), DoubleVector(doubleArrayOf(0.5, 0.5)))
            )
            val (o1, o2) = arrayOf(
                OutputNeuron(setOf(Pair(h1, 0))),
                OutputNeuron(setOf(Pair(h1, 1)))
            )
            return Network(setOf(h1), arrayOf(i1, i2), arrayOf(o1, o2))
        }

        /**
         * Build network from the sequence of bits
         */
//        fun fromBinary(representation: BitSet): Network {
//            return Network(null, null, null)
//        }
    }

}