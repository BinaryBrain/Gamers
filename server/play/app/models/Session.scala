package models

import java.security.SecureRandom
import java.sql.Timestamp
import java.util.Date
import models.mysql._

case class UserSession(token: String, userId: Int, created: Timestamp, last_access: Timestamp)

class UserSessions(tag: Tag) extends Table[UserSession](tag, "sessions") {
  def token = column[String]("token", O.PrimaryKey)
  def userId = column[Int]("user")
  def created = column[Timestamp]("created")
  def last_access = column[Timestamp]("last_access")

  def * = (token, userId, created, last_access) <> (UserSession.tupled, UserSession.unapply)

  def userFK = foreignKey("session_person_fk", userId, People)(_.id, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
}

object UserSessions extends TableQuery(new UserSessions(_)) {
  def add(userId: Int)(implicit s: Session): String = {
    val random: SecureRandom = new SecureRandom()
    val bytes = new Array[Byte](20) // TODO settings

    random.nextBytes(bytes)

    val token: String = bytes.map("%02x".format(_)).mkString
    val t: Timestamp = new Timestamp((new Date).getTime())

    UserSessions += UserSession(token, userId, t, t)

    token
  }
}
