package controllers

import actors.SimulatorSocketHandler
import play.api.mvc.{Action, WebSocket, Controller}
import play.api.Play.current

class TrafficSimulator extends Controller {

    def simulatorPage() = Action { implicit request =>
        Ok(views.html.simulator(request))
    }

    def simulatorSocket() = WebSocket.acceptWithActor[String, String] { req => out =>
        SimulatorSocketHandler.props(out)
    }

}
