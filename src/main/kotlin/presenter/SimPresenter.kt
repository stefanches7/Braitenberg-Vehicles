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
        startSimulation(worldWidth, worldHeight, vehiclesCount)
    }

    fun startSimulation(
        worldWidth: Double,
        worldHeight: Double,
        vehiclesCount: Double
    ): List<MutableSet<out Any>> {
        for (i in 1..vehiclesCount.toInt()) {
            val vehicleLength = 40.0
            val vehicleWidth = 20.0
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
        val startWorldObjects: MutableSet<WorldObject> = mutableSetOf()
        for (i in 1..5) startWorldObjects.add(WorldObject.randomWorldObject(worldWidth, worldHeight))
        model = SimModel(
            SimWorld(
                worldWidth,
                worldHeight,
                startWorldObjects
            ),
            vehicles
        )
        return (listOf(startWorldObjects, vehicles))
    }

}