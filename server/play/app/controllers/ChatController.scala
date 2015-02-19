package controllers

import java.sql.Timestamp
import java.util.Date
import akka.actor._
import models._
import play.Logger
import play.api.libs.json._
import play.api.db.slick.Config.driver.simple._
import scala.util.Try

// TODO try to avoid those ugly id parameters
object ChatController {
  def handle(id: Int, event: ChatEvent): JsValue = {
    event match {
      case event: NewMessageEvent =>
        Logger.debug(s"[$id] NewMessageEvent received (ChatCtrl): $event")
        getChat(id)
    }
  }

  def getChat(id: Int) = {
    DB.withSession { implicit session =>
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
    }
  }

  def newMessage(id: Int, content: String, typ: Int, withParticipants: Array[Int]): JsValue = {
    DB.withSession { implicit session =>
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

      EventDispatcher.broadcast(new NewMessageEvent(newMessage))

      Messages += newMessage

      participants.foreach {
        RoomParticipants !+= RoomParticipant(roomId, _)
      }

      Json.obj("cmd" -> "message-sent")
    }
  }
}
