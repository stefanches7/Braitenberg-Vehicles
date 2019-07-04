package presenter

import agent.Vehicle
import javafx.animation.Timeline
import javafx.event.EventHandler
import model.SimModel
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
        view: SimView,
        frameRate: Byte
    ) {
        this.view = view
        interval = ceil(1000F / frameRate).toInt()
        model =
            SimModel.Factory.defaultModel(
                worldWidth, worldHeight, vehicleHeight = 20.0, vehicleLength = 40.0, effectMin = 10.0,
                effectMax = 50.0, worldObjectCount = 10, vehiclesCount = vehiclesCount
            )
        this.view.renderWorld(model)
        updateRender()
    }

    /**
     * Updates render frame
     */
    fun updateRender() {
        if (running) {
            val timeline = thisTickAnimationTimeline()
            timeline.onFinished = EventHandler {
                if (gaUpdateQueued) {
                    model.nextEpoch()
                    gaUpdateQueued = false
                    fire(ModifyRenderedEvent())
                } else renderReady()
            }
            timeline.play()
        }
    }

    fun thisTickAnimationTimeline(): Timeline {
        val vehicles = model.vehicles
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

    /**
     * Queues genetic algorithm update and waits until it finishes. Returns outselected vehicles.
     * TODO block rendering to avoid concurrent accessing of elements.
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