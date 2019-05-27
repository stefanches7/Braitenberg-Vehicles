package view

import Body
import Motor
import Sensor
import Vector
import Vehicle
import WorldObject
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.layout.StackPane
import javafx.scene.shape.Shape
import tornadofx.*

class SimView : View() {
    val canvas: Group = group()
    val registeredVehiclesRender: MutableList<VehicleRender> = mutableListOf()

    override val root = stackpane {
        canvas
    }

    fun addVehicles(vehicles: Array<Vehicle>) {
        vehicles.forEach {
            val item = VehicleRender(it.body, it.motors, it.sensors)
            canvas += item
            registeredVehiclesRender += item
        }
    }

    fun addWorldObjects(wobjs: Array<WorldObject>) = wobjs.forEach { canvas += it.shape }

    /**
     * Update newCoordinatesVehicles.size first newCoordinatesVehicles in the list with animation.
     */
    fun updateVehicles(newCoordinatesVehicles: Array<Vehicle>) {
        for (i in 1..newCoordinatesVehicles.size) {

        }
    }

    class VehicleRender(b: Body, m: Array<Motor>, s: Array<Sensor>) : StackPane() {
        val element: StackPane = stackpane {
            group {
                b.shape
                m.forEach { it.shape }
                s.forEach { it.shape }
            }
        }

        fun update(velocity: Vector) {
            val bodyparts: List<Node> = element.children.filter { it is Shape }
            bodyparts.forEach {
                it.layoutXProperty().animate(it.layoutX + velocity.x, 1.seconds)
                it.layoutYProperty().animate(it.layoutY + velocity.y, 1.seconds)
            }
        }

    }

}