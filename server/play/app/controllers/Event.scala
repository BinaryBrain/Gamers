package controllers

import models._

abstract class Event

abstract class ChatEvent extends Event

case class NewMessageEvent(message: Message) extends ChatEvent
