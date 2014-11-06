package models

import scala.collection.mutable.ArraySeq
import java.util.Date
import play.api.libs.json._
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

case class Room(id: Int, name: String) {
  
}

class Chat(tag: Tag) extends Table[Room](tag, "chat") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  
  def * = (id, name) <> (Room.tupled, Room.unapply)
}

object Chat extends TableQuery(new Chat(_)) {
  def notifyCreate(answer: Person): Unit = {
  }

  def notifyUpdate(answer: Person): Unit = {
  }

  def notifyDelete(user: Int, event: Int): Unit = {
  }
}

/*
object Chat {
  val rooms = new ArraySeq[Room](0)
  
  class Room {
    val messages = new ArraySeq[Message](0)
  }
  
  class Message {
    val time: Date
    val from: Person
    val content: String
    
    implicit val implicitMessageWrites = new Writes[Message] {
      def writes(message: Message): JsValue = {
          Json.obj("time" -> time, "from" -> from, "content" -> content)
      }
    }
  }
}
*/