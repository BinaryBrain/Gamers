package models

import play.api.libs.json._
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

case class Person(id: Int, name: String, email: String, password: String) {}

class People(tag: Tag) extends Table[Person](tag, "people") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def email = column[String]("email")
  def password = column[String]("password")
  
  def * = (id, name, email, password) <> (Person.tupled, Person.unapply)
}

object People extends TableQuery(new People(_)) {
}

object me extends Person(42, "Binary Brain", "sacha.bron@copperflake.com", "love")
