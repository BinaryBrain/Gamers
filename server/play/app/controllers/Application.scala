package controllers

import java.sql.Timestamp
import java.util.Date
import play.api._
import play.api.mvc._
import play.api.libs.json.{Json, JsValue}
import akka.actor._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._

import models._

import scala.util.Try

object Application extends Controller {
  def index = Action {
    Ok(views.html.index())
  }

  def ws = WebSocket.acceptWithActor[JsValue, JsValue] {
    request =>
    out =>
      WebSocketActor.props(out)
  }

  def rest = TODO
}

object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}

class WebSocketActor(out: ActorRef) extends Actor {
  import context._

  def connected(id: Int): Receive = {
    case datagram: JsValue =>
      out ! treat(id, datagram)

    case event: ChatEvent =>
      Logger.debug(s"[$id] ChatEvent received (App): $event")
      out ! ChatController.handle(id, event)

    /*
    case message: Message =>
      Logger.debug(s"Message received: $message")
      out ! Json.obj("new message" -> "dummy")

      val content = Json.obj(
          "id" -> room
          "participants" -> chatParticipants.filter(cp => cp.room == room.id && cp.person != id).map(cp => cp.person),
          "messages" -> message
        )
        out ! Json.obj("cmd" -> "chat-update", "content" -> content)
    */
  }

  override def receive = {
    case datagram: JsValue =>
      Logger.debug(s"Datagram received: $datagram")

      val cmd = (datagram \ "cmd").as[String]
      val authOpt = (datagram \ "auth").as[Option[String]]

      authOpt match {
        case None =>
          cmd match {
            case "login" =>
              val email = (datagram \ "content" \ "email").as[String]
              val password = (datagram \ "content" \ "password").as[String]

              SessionController.login(email, password, self) match {
                case Some(userSession) =>
                  become(connected(userSession.user))

                  out ! Json.obj("cmd" -> "login-success", "content" -> Json.obj("token" -> userSession.token, "id" -> userSession.user))
                case None =>
                  out ! Json.obj("error" -> "Bad email or password")
              }

            case _ => out ! Json.obj("error" -> s"Unknown command: $cmd")
          }

        case Some(auth) =>
          DB.withSession { implicit session =>
            val id: Int = UserActiveSessions.checkToken(auth)
            become(connected(id))
          }
      }
  }

  def treat(id: Int, datagram: JsValue): JsValue = {
    DB.withSession { implicit session =>
      val cmd = (datagram \ "cmd").as[String]

      cmd match {
        case "ping" =>
          Json.obj("cmd" -> "pong")

        case "get-chat" =>
          ChatController.getChat(id)

        case "get-people" =>
          val content = People.filter(_.id =!= id).list.toArray

          Json.obj("cmd" -> "people-update", "content" -> content)

        case "new-message" =>
          val cnt = datagram \ "content"
          val msg = cnt \ "message"

          val content = (msg \ "content").as[String]
          val typ = if ((msg \ "type").as[String] == "text") 0 else 1
          val withParticipants = (cnt \ "participants").as[Array[Int]]

          ChatController.newMessage(id, content, typ, withParticipants)

        case _ => Json.obj("error" -> s"Unknown command '$cmd'")
      }
    }
  }
}

object Mutables {
  var users: Map[Int, ActorRef] = Map[Int, ActorRef]()
}
