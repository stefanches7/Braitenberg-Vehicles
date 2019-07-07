package view

import config.SimConfigItem
import presenter.SimPresenter
import tornadofx.*

class WelcomeScreen : View("Braitenberg vehicles simulation") {
    val presenter: SimPresenter by inject()
    val simconf: SimConfigItem by inject()


}
