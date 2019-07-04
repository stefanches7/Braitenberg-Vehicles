package model

import Dot
import agent.Vehicle
import agent.brain.Network
import mean
import org.nield.kotlinstatistics.WeightedCoin
import java.lang.Math.floor
import kotlin.random.Random

/**
 * Contains buisness logic of the world. Genetic algorithm part of the model is one-point crossover, elitarism and luck survival.
 */
class SimModel(
    val worldWidth: Double,
    val worldHeight: Double,
    val objects: MutableSet<WorldObject>,
    var vehicles: MutableList<Vehicle>,
    mutationRate: Double = 0.05,
    matingRate: Double = 0.05, /* how probable is it, that unique vehicle pair mates? */
    val rateEliteSelected: Double = 0.1,
    rateLuckySelected: Double = 0.05
) {

    val mutationCoin = WeightedCoin(mutationRate)
    val matingCoin = WeightedCoin(matingRate)
    val selectLuckyCoin = WeightedCoin(rateLuckySelected)
    var epochCount = 0

    /**
     * Returns vehicles active after an epoch update.
     */
    fun nextEpoch(): Collection<Vehicle> {
        this.mutateBrains()
        this.crossoverBrains()
        this.selectVehicles()
        ++epochCount
        return this.vehicles
    }

    private fun mutateBrains() {
        vehicles.forEach {
            val binBrain = it.brain.toBinary()
            for (i in 0 until binBrain.length()) {
                if (mutationCoin.flip()) binBrain.flip(i)
            }
            it.brain = Network.fromBinary(binBrain)
        }
    }

    private fun crossoverBrains() {
        //order vehicles
        val arr = vehicles.toTypedArray()
        for (m1 in 0 until arr.size) {
            for (m2 in m1 until arr.size) {
                if (matingCoin.flip()) {
                    vehicles.add(mateBrains(arr[m1].brain, arr[m2].brain))
                }
            }
        }
    }

    private fun mateBrains(brain1: Network, brain2: Network): Vehicle {
        val b1 = brain1.toBinary()
        val b2 = brain2.toBinary()
        val coPoint = Random.nextInt(b1.length())
        val childBrain = b1.crossover(b2, coPoint)
        return Vehicle.randomSimpleVehicle(
            worldHeight = worldHeight,
            worldWidth = worldWidth,
            brain = Network.fromBinary(childBrain)
        )
    }

    private fun selectVehicles() {
        val fitnessDecrVehicles =
            fitness(this.vehicles).toList().sortedBy { (_, v) -> v }.reversed().toMap().keys.toList()
        var died = mutableSetOf<Vehicle>()
        for (i in 0 until fitnessDecrVehicles.size) {
            if (i > fitnessDecrVehicles.size * rateEliteSelected && !selectLuckyCoin.flip()) died.add(
                fitnessDecrVehicles[i]
            )
        }
        this.vehicles.removeAll(died)
    }

    private fun fitness(individuals: Collection<Vehicle>): HashMap<Vehicle, Double> {
        val out = hashMapOf<Vehicle, Double>()
        individuals.forEach {
            out.put(it, mean(it.speed.vecLength(), it.oldSpeed.vecLength()))
        }
        return out
    }


    companion object Factory {
        var worldEnd = Dot(0.0, 0.0)
        /**
         * All vehicles default, have same length, default world objects.
         */
        fun defaultModel(
            worldWidth: Double = 800.00,
            worldHeight: Double = 600.0,
            vehiclesCount: Int = 10,
            vehicleLength: Double = floor(worldWidth / 80),
            vehicleHeight: Double = floor(worldHeight / 150),
            worldObjectCount: Int = 5,
            sensorsDistance: Double = vehicleHeight / 2.0,
            effectMin: Double = 10.0,
            effectMax: Double = 100.0
        ): SimModel {
            worldEnd = Dot(worldWidth, worldHeight)
            val vehicles: MutableList<Vehicle> = mutableListOf()
            for (i in 1..vehiclesCount) {
                vehicles.add(
                    Vehicle.randomSimpleVehicle(worldWidth, worldHeight, vehicleLength, vehicleHeight, sensorsDistance)
                )

            }

            val startWorldObjects: MutableSet<WorldObject> = mutableSetOf()
            val objectSize = 10.0
            for (i in 1..worldObjectCount) startWorldObjects.add(
                WorldObject.randomWorldObject(
                    worldWidth,
                    worldHeight,
                    effectMin,
                    effectMax,
                    objectSize
                )
            )
            return SimModel(
                worldWidth,
                worldHeight,
                startWorldObjects,
                vehicles
            )
        }
    }
}

