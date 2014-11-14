package models

import java.sql.Timestamp

import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

import scala.util.Try

case class Room(id: Int, participants: String) {}
case class Message(id: Int, room: Int, from: Int, `type`: Int, content: String, time: Timestamp) {}
case class ChatParticipant(room: Int, person: Int) {}

class Rooms(tag: Tag) extends Table[Room](tag, "chat_rooms") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def participants = column[String]("participants")
  
  def * = (id, participants) <> (Room.tupled, Room.unapply)
}

class Messages(tag: Tag) extends Table[Message](tag, "chat_messages") {
  val rooms = TableQuery[Rooms]
  val people = TableQuery[People]

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def roomId = column[Int]("room_id")
  def from = column[Int]("sender_id")
  def `type` = column[Int]("type")
  def content = column[String]("content")
  def date = column[Timestamp]("time")

  def * = (id, roomId, from, `type`, content, date) <> (Message.tupled, Message.unapply)

  def roomFK = foreignKey("message_room_fk", roomId, rooms)(_.id, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  def senderFK = foreignKey("message_person_fk", from, people)(_.id, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
}

class ChatParticipants(tag: Tag) extends Table[ChatParticipant](tag, "chat_participants") {
  val rooms = TableQuery[Rooms]
  val people = TableQuery[People]
  
  def roomId = column[Int]("room_id")
  def personId = column[Int]("person_id")

  def * = (roomId, personId) <> (ChatParticipant.tupled, ChatParticipant.unapply)
  
  def roomFK = foreignKey("chatparticipants_room_fk", roomId, rooms)(_.id, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  def personFK = foreignKey("chatparticipants_person_fk", personId, people)(_.id, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
}

object Rooms extends TableQuery(new Rooms(_)) {
  def !+=(room: Room)(implicit s: Session): Int = {
    val query = Rooms.filter(_.participants === room.participants)
    Try {
      Rooms returning Rooms.map(_.id) += room
    } getOrElse {
      query.first.id
    }
  }
}

object Messages extends TableQuery(new Messages(_)) {}
object ChatParticipants extends TableQuery(new ChatParticipants(_)) {
  def !+=(cp: ChatParticipant)(implicit s: Session) = {
    val query = ChatParticipants.filter(t => t.roomId === cp.room && t. personId === cp.person)
    Try {
      ChatParticipants += cp
    } getOrElse {
      query.first
    }
  }
}
