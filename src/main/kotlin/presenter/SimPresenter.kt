package presenter

import agent.Vehicle
import config.SimConfig
import javafx.animation.Timeline
import javafx.event.EventHandler
import model.SimModel
import model.WorldObject
import tornadofx.*
import view.SimView
import view.WelcomeScreen
import kotlin.math.ceil

class SimPresenter() : Controller() {
    lateinit var conf: SimConfig
    lateinit var view: SimView
    lateinit var model: SimModel
    val configView: WelcomeScreen by inject()
    var running = true
    var paused = false
    var interval: Int = 0

    private var gaUpdateQueued = false

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
    fun startSimulation(conf: SimConfig) {
        this.conf = conf
        interval = ceil(1000F / conf.fps).toInt()
        model =
            SimModel.Factory.instance(
                conf.worldWidth.toDouble(),
                conf.worldLength.toDouble(),
                effectMin = conf.minObjectEffect.toDouble(),
                effectMax = conf.maxObjectEffect.toDouble(),
                worldObjectCount = conf.objectCount.toInt(),
                startingVehicles = conf.startingAgents.toInt(),
                vehicleLength = conf.vehicleLength.toDouble(),
                vehicleHeight = conf.vehicleWidth.toDouble(),
                sensorsDistance = conf.sensorsDistance.toDouble(),
                brainSize = conf.brainSize.toInt(),
                rateLuckySelected = conf.rateLuckySelected.toDouble(),
                rateEliteSelected = conf.rateEliteSelected.toDouble(),
                matingRate = conf.matingRate.toDouble(),
                mutationRate = conf.mutationRate.toDouble(),
                presenter = this
            )
        view = SimView(this, conf.worldWidth.toDouble(), conf.worldLength.toDouble())
        configView.replaceWith(SimView::class)
        fire(UpdateRenderEvent()) //tells view to render model
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