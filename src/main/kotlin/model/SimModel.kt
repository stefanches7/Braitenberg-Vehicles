package model

import agent.Vehicle
import world.WorldObject

/**
 * Contains buisness logic of the world.
 */
class SimModel(
    val worldWidth: Double,
    val worldHeight: Double,
    val objects: MutableSet<WorldObject>,
    var vehicles: MutableSet<Vehicle>
)

