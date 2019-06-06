package presenter

import agent.Vehicle
import model.SimModel
import tornadofx.*
import view.SimView
import world.WorldObject
import kotlin.math.ceil
import kotlin.random.Random

class SimPresenter : Controller() {
    lateinit var model: SimModel
    lateinit var view: SimView
    var running = true
    var interval: Int = 0

    init {
        subscribe<RenderReadyEvent> {
            updateRender()
        }
    }

    fun startSimulation(
        worldWidth: Double,
        worldHeight: Double,
        vehiclesCount: Double,
        view: SimView,
        frameRate: Byte
    ) {
        this.view = view
        interval = ceil(1000F / frameRate).toInt()
        val vehicles: MutableSet<Vehicle> = mutableSetOf()
        for (i in 1..vehiclesCount.toInt()) {
            val vehicleLength = 40.0
            val vehicleWidth = 20.0
            vehicles.add(
                Vehicle.simpleVehicle(
                    Random.nextDouble(-worldWidth / 2, worldWidth / 2),
                    Random.nextDouble(-worldHeight / 2, worldHeight / 2),
                    vehicleWidth, vehicleLength, 1.0, 10.0,
                    Random.nextDouble(-10.0, 10.0),
                    Random.nextDouble(-10.0, 10.0)
                )
            )

        }
        val startWorldObjects: MutableSet<WorldObject> = mutableSetOf()
        for (i in 1..5) startWorldObjects.add(WorldObject.randomWorldObject(worldWidth, worldHeight))
        model = SimModel(
            worldWidth,
            worldHeight,
            startWorldObjects,
            vehicles
        )
        this.view.renderWorld(model)
        updateRender()
    }

    /**
     * Updates render frame
     */
    fun updateRender() {
        if (running) {
            val vehicles = model.vehicles
            val timeline = timeline {
                keyframe(interval.millis) {
                    vehicles.forEach {
                        it.updateVelocity(model.objects)
                        val newVelocity = it.speed
                        val bodyPartsList = it.render.list
                        bodyPartsList.forEach { bp ->
                            run {
                                this += keyvalue(bp.layoutXProperty(), bp.layoutX + newVelocity.x)
                                this += keyvalue(bp.layoutYProperty(), bp.layoutY + newVelocity.y)
                            }
                        }
                    }
                }
            }
            timeline.setOnFinished { fire(RenderReadyEvent()) }
            timeline.play()
        }
    }

}