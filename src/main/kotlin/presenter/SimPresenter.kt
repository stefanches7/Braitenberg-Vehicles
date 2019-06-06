package presenter

import agent.Vehicle
import model.SimModel
import tornadofx.*
import view.SimView
import world.WorldObject
import kotlin.math.ceil
import kotlin.random.Random

class SimPresenter() : Controller() {
    lateinit var model: SimModel
    lateinit var view: SimView
    var running = true
    var paused = false
    var interval: Int = 0

    init {
        subscribe<RenderReadyEvent> {
            if (running and !paused)
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
                    Random.nextDouble(0.0, worldWidth),
                    Random.nextDouble(0.0, worldHeight),
                    vehicleWidth, vehicleLength, 1.0, 10.0,
                    Random.nextDouble(-10.0, 10.0),
                    Random.nextDouble(-10.0, 10.0)
                )
            )

        }
        val startWorldObjects: MutableSet<WorldObject> = mutableSetOf()
        val effectMin = 10.0
        val effectMax = 100.0
        val objectSize = 10.0
        for (i in 1..20) startWorldObjects.add(
            WorldObject.randomWorldObject(
                worldWidth,
                worldHeight,
                effectMin,
                effectMax,
                objectSize
            )
        )
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
                        it.updateMovementVector(model.objects)
                        it.render.currentUpdate().forEach { kv ->
                            run {
                                this += kv
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