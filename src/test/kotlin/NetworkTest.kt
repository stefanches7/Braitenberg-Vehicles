import agent.Network
import tornadofx.*
import org.junit.jupiter.api.Test as test


class NetworkTest {

    @test
    fun `gives right output`() {
        val net = Network.smallAutoencoder()
        val motorOut =
            net.propagate(arrayOf(DoubleVector(1.0, 1.0), DoubleVector(1.0, 1.0)))
        arrayOf(
            DoubleVector(1.0, 1.0).toString(),
            DoubleVector(1.0, 1.0).toString()
        ).forEach { println(it) }
        motorOut.forEach { println(it.toString()) }

    }

    @test
    fun `run code`() { //DEBUG
        val vec = DoubleVector(2.0, 4.0)
        println("$vec, abs${vec.x * vec.x + vec.y * vec.y}")
        println("$vec, abs${vec.x * vec.x + vec.y * vec.y}")
        vec.rotate(90.deg)
        println("$vec, abs${vec.x * vec.x + vec.y * vec.y}")
    }
}