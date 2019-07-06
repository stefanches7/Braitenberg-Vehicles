package view

import agent.Vehicle
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import model.SimModel
import model.WorldObject
import presenter.RenderReadyEvent
import presenter.SimPresenter
import presenter.UpdateRenderEvent
import tornadofx.*
import kotlin.system.exitProcess

class SimView : View() {
    val presenter: SimPresenter = SimPresenter()
    val canvas: AnchorPane
    val frameRate = 10

    override val root = vbox {
        anchorpane {}
        keyboard {
            addEventFilter(KeyEvent.KEY_PRESSED) { e ->
                processInput(e.code)
            }
        }
    }


    init {
        val (worldWidth, worldHeight, startingVehicles) = arrayOf(800.0, 800.0, 10.0)
        canvas = root.children.filtered { it is AnchorPane }[0] as AnchorPane
        runAsync {
            presenter.startSimulation(
                worldWidth,
                worldHeight,
                startingVehicles.toInt(),
                view = find(SimView::class),
                frameRate = frameRate.toByte()
            )
        } ui { }
        subscribe<UpdateRenderEvent> {
            if (!canvas.getChildList()!!.any { it is WorldObjectGroup }) renderWorldObjects(presenter.getCurrentWorldObjects())
            renderVehicles(presenter.getCurrentVehicles())
            fire(RenderReadyEvent())
        }
    }


    fun processInput(code: KeyCode) {
        when (code) {
            KeyCode.ESCAPE -> {
                presenter.running = false
                exitProcess(0)
            }
            KeyCode.SPACE -> {
                if (presenter.paused) fire(RenderReadyEvent())
                else presenter.pause()
            }
            in arrayOf(KeyCode.RIGHT, KeyCode.KP_RIGHT) -> {
                runAsync {
                    presenter.queueEpochUpdate()
                } ui {}
            }
            else -> Unit
        }
    }

    fun renderWorld(model: SimModel) {
        renderVehicles(model.vehicles)
        renderWorldObjects(model.objects)
    }

    /**
     * Adds vehicles shapes to the simulation.
     */
    fun renderVehicles(
        vehicles: Collection<Vehicle>
    ) {
        this.canvas.getChildList()?.removeIf { it !is WorldObjectGroup }
        vehicles.forEach {
            with(canvas) {
                it.bodyParts.forEach { bp ->
                    this += bp.shape
                }
            }
        }
    }

    fun renderWorldObjects(wobjs: MutableCollection<WorldObject>) = wobjs.forEach {
        //TODO
        with(canvas) {
            this += it.shape
        }
    }

    companion object {
        val renderLock = Any()
    }
}