package models

import scala.collection.mutable.ArraySeq
import play.api.libs.json._
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current
import java.sql.Date

case class Room(id: Int) {}
case class Message(id: Int, roomId: Int, senderId: Int, `type`: Int, content: String, date: Date) {}
case class ChatParticipant(roomId: Int, personId: Int) {}

class Chat(tag: Tag) extends Table[Room](tag, "chat") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  
  def * = (id) <> (Room, Room.unapply)
}

class Messages(tag: Tag) extends Table[Message](tag, "messages") {
  val rooms = TableQuery[Chat]
  val people = TableQuery[People]

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def roomId = column[Int]("room")
  def senderId = column[Int]("sender")
  def `type` = column[Int]("type")
  def content = column[String]("content")
  def date = column[Date]("date")
  
  def room = foreignKey("room_fk", roomId, rooms)(_.id, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  def sender = foreignKey("person_fk", senderId, people)(_.id, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.SetNull)
  
  def * = (id, roomId, senderId, `type`, content, date) <> (Message.tupled, Message.unapply)
}

class ChatParticipants(tag: Tag) extends Table[ChatParticipant](tag, "chat_participants") {
  val rooms = TableQuery[Chat]
  val people = TableQuery[People]
  
  def roomId = column[Int]("room", O.PrimaryKey)
  def personId = column[Int]("sender", O.PrimaryKey)
  
  def room = foreignKey("room_fk", roomId, rooms)(_.id, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  def person = foreignKey("person_fk", personId, people)(_.id, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.SetNull)
  
  def * = (roomId, personId) <> (ChatParticipant.tupled, ChatParticipant.unapply)
}

object Chat extends TableQuery(new Chat(_)) {}
object Messages extends TableQuery(new Messages(_)) {}
object ChatParticipants extends TableQuery(new ChatParticipants(_)) {}
