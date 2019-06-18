package agent

import DoubleVector
import java.util.*

open class Network(
    val innerNeurons: Set<NormalizedInnerNeuron>,
    val inputNeurons: Array<NormalizedInputNeuron>,
    val outputNeurons: Array<OutputNeuron>
) {

    /**
     * Propagate the signal and get the vector output for motors
     */
    fun propagate(signal: Array<DoubleVector>): Array<DoubleVector> {
        check(signal.size == inputNeurons.size) {
            throw IllegalArgumentException("Signal dimension must be equal to the input layer dimension!")
        }
        signal.withIndex().forEach { (idx, e) ->
            this.inputNeurons[idx].signal = e
        }
        //TODO remove ugly unpacking
        val (out) = outputNeurons.map { it.processSignal() }.toTypedArray()
        return out
    }

    /**
     * Binary representation of network.
     */
    fun toBinary(): BitSet {
        //TODO not implemented
        return BitSet()
    }

    companion object Factory {

        /**
         * 1 hidden layer, 2*2d input and output.
         */
        fun smallAutoencoder(): Network {
            val (i1, i2) = arrayOf(
                NormalizedInputNeuron(
                    arrayOf(
                        DoubleVector(
                            1.0, 1.0
                        )
                    )
                ),
                NormalizedInputNeuron(
                    arrayOf(
                        DoubleVector(1.0, 1.0)
                    )
                )
            )
            val h1 = NormalizedInnerNeuron(
                setOf(Pair(i1, 0), Pair(i2, 0)), arrayOf(DoubleVector(0.6, 0.4), DoubleVector(0.4, 0.6))
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