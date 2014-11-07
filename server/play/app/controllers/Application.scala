package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.JsValue
import akka.actor._
import play.api.Play.current
import play.api.libs.json.JsObject
import play.api.libs.json.Json

import models._

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
      case "get-chat" =>
        Json.obj("cmd" -> "chat-update", "content" -> Json.parse("""
        [
          {
            "id":1,
            "participants":[
              {
                "id":1,
                "name":"Jean-Jean"
              },
              {
                "id":2,
                "name":"xXx Dark sombre xXx"
              },
              {
                "id":3,
                "name":"Tabi Nah"
              },
              {
                "id":42,
                "name":"Binary Brain"
              }
            ],
            "messages":[
              {
                "time":"2014-10-19T17:09:22.695Z",
                "from":1,
                "type":"text",
                "content":"Salut!"
              },
              {
                "time":"2014-10-20T17:15:21.687Z",
                "from":2,
                "type":"text",
                "content":"yop"
              },
              {
                "time":"2014-10-20T17:15:36.725Z",
                "from":42,
                "type":"text",
                "content":"pouldre"
              },
              {
                "time":"2014-10-20T17:15:52.695Z",
                "from":3,
                "type":"text",
                "content":"fnu"
              },
              {
                "time":"2014-10-20T17:15:57.687Z",
                "from":2,
                "type":"text",
                "content":"ca roule?"
              },
              {
                 "time":"2014-10-19T17:16:22.695Z",
                 "from":1,
                 "type":"text",
                 "content":"Voui!"
              },
              {
                "time":"2014-10-20T17:16:58.725Z",
                "from":42,
                "type":"text",
                "content":"yep"
              }
            ]
          },
          {
            "id":3,
            "participants":[
              {
                "id":18,
                "name":"Slalutrin"
              },
              {
                "id":42,
                "name":"Binary Brain"
              }
            ],
            "messages":[
              {
                "time":"2014-10-21T14:35:04.850Z",
                "from":14,
                "type":"text",
                "content":"pouldre"
              },
              {
                "time":"2014-10-21T14:35:14.750Z",
                "from":42,
                "type":"text",
                "content":"fnu"
              }
            ]
          }
        ]
        """))
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
        
        case "new-message" =>
          
          Json.obj("cmd" -> "message-sent")
      
      case _ => Json.obj("error" -> s"Unknown command '$cmd'")
    }
  }
}
