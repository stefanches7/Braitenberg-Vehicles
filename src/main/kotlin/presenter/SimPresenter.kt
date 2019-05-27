package presenter

import SimConfig
import SimModel
import SimWorld
import Vehicle
import WorldObject
import tornadofx.*
import kotlin.random.Random

class SimPresenter : Controller() {
    lateinit var model: SimModel
    val vehicles: MutableSet<Vehicle> = mutableSetOf()

    fun startSimulation(config: SimConfig) {
        // TODO fetch world layout from external source/own class
        val (worldWidth, worldHeight, vehiclesCount) = config.parse()
        for (i in 1..vehiclesCount.toInt()) {
            val vehicleLength: Double = 40.0
            val vehicleWidth: Double = 20.0
            vehicles.add(
                Vehicle.simpleVehicle(
                    Random.nextDouble(-worldWidth / 2, worldWidth / 2),
                    Random.nextDouble(-worldHeight / 2, worldHeight / 2),
                    vehicleLength, vehicleWidth, 1.0, 10.0,
                    Random.nextDouble(-worldWidth / 2, worldWidth / 2),
                    Random.nextDouble(-worldWidth / 2, worldWidth / 2)
                )
            )

        }

        model = SimModel(
            SimWorld(
                config.worldWidth?.toDoubleOrNull() ?: 1000.0,
                config.worldLength?.toDoubleOrNull() ?: 1000.0,
                mutableSetOf(
                    WorldObject(
                        Random.nextDouble(worldWidth),
                        Random.nextDouble(worldHeight),
                        Random.nextDouble(10.0),
                        Random.nextDouble(10.0)
                    )
                )
            ),
            vehicles
        )

    }

}