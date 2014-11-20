package models

import java.sql.Timestamp

import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

import scala.util.Try

case class Room(id: Int, participants: String) {}
case class Message(id: Int, room: Int, from: Int, `type`: Int, content: String, time: Timestamp) {}
case class RoomParticipant(room: Int, person: Int) {}

class Rooms(tag: Tag) extends Table[Room](tag, "chat_rooms") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def participants = column[String]("participants")
  
  def * = (id, participants) <> (Room.tupled, Room.unapply)
}

class Messages(tag: Tag) extends Table[Message](tag, "chat_messages") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def roomId = column[Int]("room_id")
  def from = column[Int]("sender_id")
  def `type` = column[Int]("type")
  def content = column[String]("content")
  def date = column[Timestamp]("time")

  def * = (id, roomId, from, `type`, content, date) <> (Message.tupled, Message.unapply)

  def roomFK = foreignKey("message_room_fk", roomId, Rooms)(_.id, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  def senderFK = foreignKey("message_person_fk", from, People)(_.id, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
}

class RoomParticipants(tag: Tag) extends Table[RoomParticipant](tag, "chat_room_participants") {
  def roomId = column[Int]("room_id")
  def personId = column[Int]("person_id")

  def * = (roomId, personId) <> (RoomParticipant.tupled, RoomParticipant.unapply)

  def roomFK = foreignKey("room_participant_room_fk", roomId, Rooms)(_.id, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  def personFK = foreignKey("room_participant_person_fk", personId, People)(_.id, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
}

object Rooms extends TableQuery(new Rooms(_))

object Messages extends TableQuery(new Messages(_))

object RoomParticipants extends TableQuery(new RoomParticipants(_)) {
  def !+=(rp: RoomParticipant)(implicit s: Session) = {
    val query = RoomParticipants.filter(t => t.roomId === rp.room && t. personId === rp.person)
    Try {
      query.first
    } getOrElse {
      RoomParticipants += rp
    }
  }
}
