package controllers

import java.sql.{Timestamp, Date}

import org.joda.time.DateTime
import play.api._
import play.api.mvc._
import play.api.libs.json.JsValue
import akka.actor._
import play.api.Play.current
import play.api.libs.json.Json
import play.api.db.slick.Config.driver.simple._

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
    case msg: JsValue =>
      Logger.debug(s"Message received: $msg")

      out ! treat(msg)
  }

  def treat(msg: JsValue): JsValue = {
    val cmd = (msg \ "cmd").as[String]

    val auth = (msg \ "auth").as[Option[String]]

    auth match {
      case None =>
        cmd match {
          // TODO check creditentials
          case "login" =>
            DB.withSession { implicit session =>
              val token: String = UserSessions.add(me.id)
              Json.obj("cmd" -> "login-success", "content" -> Json.obj("token" -> token))
            }

          case _ => Json.obj("error" -> s"Not logged in or unknown command '$cmd'")
        }

      case _ =>
        cmd match {
          case "get-chat" =>
            DB.withSession { implicit session =>

              val rooms: List[Room] = (Rooms join ChatParticipants.filter(_.personId === me.id) on (_.id === _.roomId)).map(_._1).list
              val chatParticipants: List[ChatParticipant] = ChatParticipants.list
              val messages: List[Message] = Messages.list

              val content = rooms.map {
                room => Json.obj(
                  "id" -> room.id,
                  "participants" -> chatParticipants.filter(cp => cp.room == room.id).map(cp => cp.person),
                  "messages" -> messages.filter(m => m.room == room.id)
                )
              }

              Json.obj("cmd" -> "chat-update", "content" -> content)
            }

          case "get-people" =>
            DB.withSession { implicit session =>
              val content = People.list.toArray

              Json.obj("cmd" -> "people-update", "content" -> content)
            }

          case "new-message" =>
            val cnt = msg \ "content"
            val participants = (cnt \ "room").as[Array[Int]]
            val from = (cnt \ "from").as[Int]
            val time = (cnt \ "time").as[Timestamp]
            val typ = if ((cnt \ "type").as[String] == "text") 0 else 1
            val content = (cnt \ "content").as[String]

            DB.withSession { implicit session =>
              val room = Room(0, participants.mkString(","))
              val id = Rooms !+= room

              val newMessage = Message(0, id, from, typ, content, time)
              Messages += newMessage

              participants.foreach {
                ChatParticipants !+= ChatParticipant(id, _)
              }
            }

            Json.obj("cmd" -> "message-sent")

          case _ => Json.obj("error" -> s"Unknown command '$cmd'")
        }
    }
  }
}
