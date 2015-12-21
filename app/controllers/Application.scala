package controllers

import actors.WebSocketHandler
import play.api.Play.current
import play.api.mvc._

class Application extends Controller {

    def index = Action {
        //Ok(views.html.index("Your new application is ready."))
        Ok(views.html.celltowers())
    }

    /*
    def celltowers = Action {
        Ok(views.html.celltowers())
    }
    */

    def celltowers = WebSocket.acceptWithActor[String, String] { req => out =>
        WebSocketHandler.props(out)
    }

}
