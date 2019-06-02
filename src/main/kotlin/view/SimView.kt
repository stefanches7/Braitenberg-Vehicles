package view

import Vehicle
import WorldObject
import presenter.SimPresenter
import tornadofx.*

class SimView : View() {
    val presenter: SimPresenter = find(SimPresenter::class)
    val canvas = canvas { }
    val registeredVehiclesRender: MutableList<Vehicle.VehicleRender> = mutableListOf()

    override val root = stackpane {
        canvas
    }

    init {
        var (startWorldObjects, startVehicles) = presenter.startSimulation(400.0, 400.0, 10.0)
        startWorldObjects = startWorldObjects as MutableSet<WorldObject>
        //renderVehicles(startVehicles as MutableSet<Vehicle>)
        renderWorldObjects(startWorldObjects)
        with(canvas) {
            this += startWorldObjects.elementAt(1).shape
        }
    }

    fun renderVehicles(vehicles: MutableCollection<Vehicle>) {
        vehicles.forEach {
            if (!registeredVehiclesRender.contains(it.render)) { //add new
                with(canvas) { this += it.render.element }
                registeredVehiclesRender.add(it.render)
            } else {
                it.render.update(it.speed)
            }
        }
    }

    fun renderWorldObjects(wobjs: MutableCollection<WorldObject>) = wobjs.forEach {
        with(canvas) {
            this += it.shape
        }
    }
}