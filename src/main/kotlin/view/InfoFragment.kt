package view

import data.SimInfo
import javafx.scene.Node
import presenter.SimPresenter
import tornadofx.*

class InfoFragment<T>(infos: T, title: String? = "", icon: Node? = null) : Fragment(title, icon) {

    val presenter: SimPresenter by inject()

    override val root = vbox {

    }

    init {
        when (infos) {
            is SimInfo -> addSimInfo(infos.vehiclesCount)
            else -> Unit
        }
    }

    fun addSimInfo(vehiclesCount: Int) {
        with(root) {
            this += label("Vehicles count: $vehiclesCount\n")
        }
    }
}