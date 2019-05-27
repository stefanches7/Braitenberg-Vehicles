package view

import Vehicle
import javafx.scene.Group
import javafx.scene.layout.StackPane
import tornadofx.*

class SimView : View() {
    val canvas: Group = group()
    var registeredVehicles: Array<StackPane> = arrayOf()

    override val root = stackpane {
        canvas
    }

    fun addVehicles(vehicles: Array<Vehicle>) {
        vehicles.forEach {
            val item = stackpane {
                group {
                    it.body
                    it.motors
                    it.sensor
                }
            }
            canvas += item
            registeredVehicles += item
        }
    }

    /**
     * Update vehicles.size first vehicles in the list with animation.
     */
    fun updateVehicles(vehicles: Array<Vehicle>) {

    }

}