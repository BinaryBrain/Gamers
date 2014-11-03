package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.JsValue
import akka.actor._
import play.api.Play.current
import play.api.libs.json.JsObject
import play.api.libs.json.Json

object Application extends Controller {
  def index = Action {
    Ok(views.html.index())
  }

  def ws = WebSocket.acceptWithActor[JsValue, JsValue] { request =>
    out =>
      MyWebSocketActor.props(out)
  }
  
  def rest = TODO
}

object MyWebSocketActor {
  def props(out: ActorRef) = Props(new MyWebSocketActor(out))
}

class MyWebSocketActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: JsValue => {
      Logger.debug(s"Message received: $msg")
      
      out ! treat(msg)
    }
  }

  def treat(msg: JsValue): JsValue = {
    val cmd = (msg \ "cmd").as[String]
    
    cmd match {
      case "get-people" =>
        Json.obj("cmd" -> "people-update", "content" -> Json.parse("""
        [
          {
            "id": 1,
            "name": "Jean-Jean"
          },
          {
            "id": 2,
            "name": "xXx Dark sombre xXx"
          },
          {
            "id": 3,
            "name": "Tabi Nah"
          },
          {
            "id": 4,
            "name": "SuperMool"
          },
          {
            "id": 5,
            "name": "Jayce"
          },
          {
            "id": 7,
            "name": "Pascal"
          },
          {
            "id": 14,
            "name": "octopus82"
          },
          {
            "id": 18,
            "name": "Slalutrin"
          },
          {
            "id": 13,
            "name": "YuGuiYooo13"
          }
        ]
        """))
        
      case _ => Json.obj("error" -> s"Unknown command $cmd")
    }
  }
}
