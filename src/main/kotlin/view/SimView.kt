package view

import agent.Vehicle
import fac
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Priority
import model.SimModel
import presenter.SimPresenter
import tornadofx.*
import world.WorldObject

class SimView : View() {
    val presenter: SimPresenter = SimPresenter()
    val canvas: AnchorPane
    val registeredVehiclesRender: MutableList<Vehicle.VehicleRender> = mutableListOf()
    val frameRate = 20

    override val root = vbox {
        anchorpane {
            anchorpaneConstraints {
                topAnchor = 0.0
                bottomAnchor = 0.0
                rightAnchor = 0.0
                leftAnchor = 0.0
            }
        }
        vboxConstraints {
            vgrow = Priority.ALWAYS
        }
    }

    init {
        val (worldWidth, worldHeight, startingVehicles) = arrayOf(1000.0, 1000.0, 10.0)
        canvas = root.children[0] as AnchorPane
        canvas.setOnKeyPressed { presenter.running = false }
        runAsync {
            presenter.startSimulation(
                worldWidth,
                worldHeight,
                startingVehicles,
                find(SimView::class),
                frameRate.toByte()
            )
        } ui {
        }
        fac(5)
    }

    fun renderWorld(model: SimModel) {
        renderVehicles(model.vehicles)
        renderWorldObjects(model.objects)
    }


    /**
     * Adds vehicles shapes to the simulation.
     */
    fun renderVehicles(
        vehicles: Set<Vehicle>
    ) { //TODO
        vehicles.forEach {
            if (!registeredVehiclesRender.contains(it.render)) { //add new
                with(canvas) {
                    it.render.list.forEach { bp ->
                        this += bp
                    }
                }
                registeredVehiclesRender.add(it.render)
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