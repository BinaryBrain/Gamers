package models

import java.security.SecureRandom
import java.sql.Timestamp
import java.util.Date
import models.mysql._

case class UserActiveSession(token: String, user: Int, created: Timestamp, last_access: Timestamp)

class UserActiveSessions(tag: Tag) extends Table[UserActiveSession](tag, "sessions") {
  def token = column[String]("token", O.PrimaryKey)
  def userId = column[Int]("user")
  def created = column[Timestamp]("created")
  def last_access = column[Timestamp]("last_access")

  def * = (token, userId, created, last_access) <> (UserActiveSession.tupled, UserActiveSession.unapply)

  def userFK = foreignKey("session_person_fk", userId, People)(_.id, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
}

object UserActiveSessions extends TableQuery(new UserActiveSessions(_)) {
  def add(userId: Int)(implicit s: Session): UserActiveSession = {
    val random: SecureRandom = new SecureRandom()
    val bytes = new Array[Byte](32) // TODO put in settings

    random.nextBytes(bytes)

    val token: String = bytes.map("%02x".format(_)).mkString
    val t: Timestamp = new Timestamp((new Date).getTime)

    val session = UserActiveSession(token, userId, t, t)
    UserActiveSessions += session

    session
  }

  def checkToken(auth: String)(implicit s: Session): Int = {
    UserActiveSessions.filter(_.token === auth).map(_.userId).first
  }
}
