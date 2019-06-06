package presenter

import agent.Vehicle
import javafx.beans.property.DoubleProperty
import javafx.scene.Node
import javafx.scene.shape.Shape
import model.SimModel
import tornadofx.*
import view.SimView
import world.WorldObject
import kotlin.math.ceil
import kotlin.random.Random

class SimPresenter : Controller() {
    lateinit var model: SimModel
    lateinit var view: SimView
    var running = true
    var interval: Int = 0

    init {
        subscribe<RenderReadyEvent> {
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
                    Random.nextDouble(-worldWidth / 2, worldWidth / 2),
                    Random.nextDouble(-worldHeight / 2, worldHeight / 2),
                    vehicleLength, vehicleWidth, 1.0, 10.0,
                    Random.nextDouble(-worldWidth / 2, worldWidth / 2),
                    Random.nextDouble(-worldWidth / 2, worldWidth / 2)
                )
            )

        }
        val startWorldObjects: MutableSet<WorldObject> = mutableSetOf()
        for (i in 1..5) startWorldObjects.add(WorldObject.randomWorldObject(worldWidth, worldHeight))
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
            val propertyToEndValue: MutableMap<DoubleProperty, Double> = mutableMapOf()
            vehicles.forEach {
                it.updateVelocity(model.objects)
                val velocity = it.speed
                val bodyparts: List<Node> = it.render.element.children.filter { bp -> bp is Shape }
                // Movement animation.

                bodyparts.forEach { part ->
                    propertyToEndValue[part.layoutXProperty()] = part.layoutX + velocity.x
                    propertyToEndValue[part.layoutXProperty()] = part.layoutY + velocity.y
                }
            }
            val timeline = timeline {
                keyframe(interval.millis) {
                    propertyToEndValue.forEach { (t, u) -> this += keyvalue(t, u) }
                }
            }
            timeline.setOnFinished { fire(RenderReadyEvent()) }
            timeline.play()
        }
    }

}