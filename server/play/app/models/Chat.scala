package models

import scala.collection.mutable.ArraySeq
import play.api.libs.json._
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import java.sql.Date

case class Room(id: Int, participants: String) {}
case class Message(id: Int, roomId: Int, senderId: Int, `type`: Int, content: String, date: Date) {}
case class ChatParticipant(roomId: Int, personId: Int) {}

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
  def senderId = column[Int]("sender_id")
  def `type` = column[Int]("type")
  def content = column[String]("content")
  def date = column[Date]("date")

  def * = (id, roomId, senderId, `type`, content, date) <> (Message.tupled, Message.unapply)

  def roomFK = foreignKey("message_room_fk", roomId, rooms)(_.id, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  def senderFK = foreignKey("message_person_fk", senderId, people)(_.id, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
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
  def insertIfNotExists(room: Room)(implicit s: Session): Int = {
    val exist = Rooms.filter(_.participants === room.participants).exists.run
    if(!exist) {
      Rooms returning Rooms.map(_.id) += room
    } else {
      Rooms.filter(_.participants === room.participants).first.id
    }
  }
}

object Messages extends TableQuery(new Messages(_)) {}
object ChatParticipants extends TableQuery(new ChatParticipants(_)) {}
