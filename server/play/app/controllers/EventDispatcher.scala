package controllers

import akka.actor._
import models._
import play.Logger

object EventDispatcher {
  def register(id: Int, listener: ActorRef) = Mutables.users += (id -> listener)
  def unregister(id: Int) = Mutables.users -= id

  def broadcast(event: Event): Unit = {
    Logger.debug(s"Event received: $event")
    Mutables.users.par.foreach(_._2 ! event)
  }
}
