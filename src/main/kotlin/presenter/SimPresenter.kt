package presenter

import agent.Vehicle
import javafx.animation.Timeline
import javafx.event.EventHandler
import model.SimModel
import model.WorldObject
import tornadofx.*
import view.SimView
import kotlin.math.ceil

class SimPresenter : Controller() {
    private var gaUpdateQueued = false
    lateinit var model: SimModel
    lateinit var view: SimView
    var running = true
    var paused = false
    var interval: Int = 0

    init {
        subscribe<RenderReadyEvent> {
            if (running and !paused) {
                updateRender()
            }
        }
    }

    /**
     * Create world, starting vehicles & launch the rendering process.
     */
    fun startSimulation(
        worldWidth: Double,
        worldHeight: Double,
        vehiclesCount: Int,
        effectMin: Double = 10.0,
        effectMax: Double = 50.0,
        view: SimView,
        frameRate: Byte
    ) {
        this.view = view
        interval = ceil(1000F / frameRate).toInt()
        model =
            SimModel.Factory.defaultModel(
                worldWidth, worldHeight, effectMin = effectMin,
                effectMax = effectMax, worldObjectCount = 10, vehiclesCount = vehiclesCount
            )
        fire(UpdateRenderEvent())
    }

    /**
     * Updates render frame
     */
    fun updateRender() {
        if (running) {
            val timeline = thisTickAnimation()
            timeline.onFinished = EventHandler {
                if (gaUpdateQueued) {
                    model.nextEpoch()
                    gaUpdateQueued = false
                    fire(UpdateRenderEvent())
                } else renderReady()
            }
            timeline.play()
        }
    }

    fun thisTickAnimation(): Timeline {
        val vehicles = getCurrentVehicles()
        return timeline {
            keyframe(interval.millis) {
                vehicles.forEach {
                    it.calcCurrentUpdate(model.objects).forEach { kv ->
                        run {
                            this += kv
                        }
                    }
                }
            }
        }
    }

    fun getCurrentVehicles(): Collection<Vehicle> {
        return model.vehicles
    }

    fun getCurrentWorldObjects(): MutableSet<WorldObject> {
        return model.objects
    }

    /**
     * Queues genetic algorithm update and waits until it finishes. Returns outselected vehicles.
     */
    fun queueEpochUpdate() {
        pause()
        gaUpdateQueued = true
    }

    fun pause() {
        paused = true
    }

    fun unpause() {
        paused = false
    }

    fun renderReady() {
        paused = false
        fire(RenderReadyEvent())
    }
}