package view

import data.SimInfo
import data.VehicleInfo
import javafx.embed.swing.SwingNode
import javafx.scene.Node
import org.knowm.xchart.*
import presenter.SimPresenter
import tornadofx.*
import org.knowm.xchart.style.Styler
import java.util.*
import javax.swing.JComponent


/**
 * Fragment containing various informations about the simulation current state.
 */
class InfoFragment<T>(infos: T, title: String? = "", icon: Node? = null) : Fragment(title, icon) {

    val presenter: SimPresenter by inject()
    val swingNode = SwingNode()

    override val root = vbox {
        this.add(swingNode)
    }

    init {
        when (infos) {
            is SimInfo -> renderSimInfo(infos)
            is VehicleInfo -> renderVehicleInfo(infos)
            else -> Unit
        }
    }

    private fun renderVehicleInfo(infos: VehicleInfo) {
        with(root) {
            this += label(infos.toString())
        }
    }

    fun renderSimInfo(infos: SimInfo) {
        with(root) {
            this += label(infos.vehiclesCount.toString())
            swingNode.content = vehicleSpeedDistrChart(infos)
        }
    }

    fun vehicleSpeedDistrChart(infos: SimInfo): JComponent {
        val chart = CategoryChartBuilder()
            .width(800).height(600)
            .title("Score Histogram")
            .xAxisTitle("Score")
            .yAxisTitle("Number")
            .theme(Styler.ChartTheme.GGPlot2).build()
        val hist = Histogram(infos.vehicleSpeeds.map { it.x }, 40, -10.0, 10.0)
        chart.addSeries("vehicle speeds", hist.getxAxisData(), hist.getyAxisData())
        return XChartPanel(chart)
    }

    override fun onUndock() {
        super.onUndock()
        presenter.renderReady()
    }
}