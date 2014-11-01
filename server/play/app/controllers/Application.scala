package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.JsValue
import akka.actor._
import play.api.Play.current

object Application extends Controller {
  def index = Action {
    Ok(views.html.index())
  }
  
  def websocket = WebSocket.acceptWithActor[String, String] { request => out =>
    MyWebSocketActor.props(out)
  }
}

object MyWebSocketActor {
  def props(out: ActorRef) = Props(new MyWebSocketActor(out))
}

class MyWebSocketActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: String =>
      out ! ("I received your message: " + msg)
  }
}