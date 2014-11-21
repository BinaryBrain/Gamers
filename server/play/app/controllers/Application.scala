package controllers

import java.sql.Timestamp
import java.util.Date
import play.api._
import play.api.mvc._
import play.api.libs.json.JsValue
import akka.actor._
import play.api.Play.current
import play.api.libs.json.Json
import play.api.db.slick.Config.driver.simple._

import models._

import scala.util.Try

object Application extends Controller {
  def index = Action {
    Ok(views.html.index())
  }

  def ws = WebSocket.acceptWithActor[JsValue, JsValue] { request =>
    out =>
      WebSocketActor.props(out)
  }

  def rest = TODO
}

object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}

class WebSocketActor(out: ActorRef) extends Actor {
  def receive = {
    case datagram: JsValue =>
      Logger.debug(s"Datagram received: $datagram")

      out ! treat(datagram)
  }

  def treat(datagram: JsValue): JsValue = {
    DB.withSession { implicit session =>
      val cmd = (datagram \ "cmd").as[String]

      val authOpt = (datagram \ "auth").as[Option[String]]

      authOpt match {
        case None =>
          cmd match {

            // TODO check creditentials
            case "login" =>
              val email = (datagram \ "content" \ "email").as[String]
              val password = (datagram \ "content" \ "password").as[String]

              People.checkAuth(email, password) match {
                case Some(person) =>
                  val token: String = UserSessions.add(person.id)
                  Json.obj("cmd" -> "login-success", "content" -> Json.obj("token" -> token, "me" -> person))
                case None =>
                  Json.obj("error" -> "Bad email or password")
              }

            case _ => Json.obj("error" -> s"Not logged in or unknown command '$cmd'")
          }

        case Some(auth) =>
          val id: Int = UserSessions.checkToken(auth)

          cmd match {
            case "get-chat" =>
              val rooms: List[Room] = (Rooms join RoomParticipants.filter(_.personId === id) on (_.id === _.roomId)).map(_._1).list
              val chatParticipants: List[RoomParticipant] = RoomParticipants.list
              val messages: List[Message] = Messages.list

              val content = rooms.map {
                room => Json.obj(
                  "id" -> room.id,
                  "participants" -> chatParticipants.filter(cp => cp.room == room.id && cp.person != id).map(cp => cp.person),
                  "messages" -> messages.filter(m => m.room == room.id)
                )
              }

              Json.obj("cmd" -> "chat-update", "content" -> content)

            case "get-people" =>
              val content = People.filter(_.id =!= id).list.toArray

              Json.obj("cmd" -> "people-update", "content" -> content)

            case "new-message" =>
              val cnt = datagram \ "content"
              val msg = cnt \ "message"
              val typ = if ((msg \ "type").as[String] == "text") 0 else 1
              val withParticipants = (cnt \ "participants").as[Array[Int]]
              val content = (msg \ "content").as[String]

              val time = new Timestamp((new Date).getTime)
              val from = id
              val participants = withParticipants :+ from

              val room = Room(0, participants.sortWith(_ < _).mkString(","))

              if (withParticipants.length == 1 && withParticipants.apply(0) == id) {
                return Json.obj("error" -> "You cannot send messages to yourself.")
              }

              val roomId = /* if (participants.length == 2) { */
                Try {
                  // If the room already exists
                  Rooms.filter(_.participants === room.participants).map(_.id).first
                } getOrElse {
                  Rooms returning Rooms.map(_.id) += room
                }
                /*
                } else {
                  val roomIdOpt = (cnt \ "new-room").as[Option[Int]]

                  roomIdOpt match {
                    case Some(roomId) =>
                      if (RoomParticipants.filter(rp => rp.personId === id && rp.roomId === roomId).exists.run) {

                      } else {
                        // Error! Guy is not in the room! Haxxor spotted!
                      }
                    case None =>
                      Rooms returning Rooms.map(_.id) += room
                  }
                  // if !newRoom
                    // if id is in room,
                      // add message
                    // else
                      // error!
                  // else,
                    // add room, add message
                }
                */

              val newMessage = Message(0, roomId, from, typ, content, time)
              Messages += newMessage

              participants.foreach {
                RoomParticipants !+= RoomParticipant(roomId, _)
              }

              Json.obj("cmd" -> "message-sent")

            case _ => Json.obj("error" -> s"Unknown command '$cmd'")
          }
      }
    }
  }
}
