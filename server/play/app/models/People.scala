package models

import play.api.libs.json._
import play.api.db.slick.Config.driver.simple._
import play.api.Play.current

case class Person(id: Int, name: String) {}

class People(tag: Tag) extends Table[Person](tag, "people") {
  def id = column[Int]("id", O.PrimaryKey)
  def name = column[String]("name")
  
  def * = (id, name) <> (Person.tupled, Person.unapply)
}

object People extends TableQuery(new People(_)) {
}
