package view

import agent.Vehicle
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.shape.Line
import model.WorldObject
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
        val (worldWidth, worldHeight, startingVehicles) = arrayOf(800.0, 800.0, 100.0)
        canvas = root.children.filtered { it is AnchorPane }[0] as AnchorPane
        with(canvas) {
            this += Line(worldWidth, 0.0, worldWidth, worldHeight)
            this += Line(0.0, worldHeight, worldWidth, worldHeight)
        }
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
            presenter.renderReady()
        }
    }


    fun processInput(code: KeyCode) {
        when (code) {
            KeyCode.ESCAPE -> {
                presenter.running = false
                exitProcess(0)
            }
            KeyCode.SPACE -> {
                if (presenter.paused) presenter.renderReady()
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

    /**
     * Adds vehicles shapes to the simulation.
     */
    fun renderVehicles(
        vehicles: Collection<Vehicle>
    ) {
        this.canvas.getChildList()?.removeIf { it is VehicleGroup }
        vehicles.forEach {
            with(canvas) {
                this += it.render
            }
        }
    }

    fun renderWorldObjects(wobjs: MutableCollection<WorldObject>) = wobjs.forEach {
        //TODO
        with(canvas) {
            this += it.shape
        }
    }
}