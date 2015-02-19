package controllers

import akka.actor.ActorRef
import models._
import play.api.libs.json._
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

object SessionController {
  // TODO check creditentials
  def login(email: String, password: String, actor: ActorRef): Option[UserActiveSession] = {
    DB.withSession { implicit session =>
      People.checkAuth(email, password) match {
        case Some(person) =>
          // register in the chat
          EventDispatcher.register(person.id, actor)

          Option(UserActiveSessions.add(person.id))
        case None => None
      }
    }
  }
}
