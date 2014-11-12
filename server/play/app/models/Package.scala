import java.sql.Timestamp
import java.text.SimpleDateFormat
import play.api.Play.current
import play.api.libs.json._

package object models {
  val DB = play.api.db.slick.DB
  val mysql = scala.slick.driver.MySQLDriver.simple
  val sql = scala.slick.jdbc.StaticQuery

  implicit object timestampFormat extends Format[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    def reads(json: JsValue) = JsSuccess(new Timestamp(format.parse(json.as[String]).getTime))
    def writes(ts: Timestamp) = JsString(format.format(ts))
  }

  implicit val personJsonFormat = Json.format[Person]
  implicit val roomJsonFormat = Json.format[Room]
  implicit val messageJsonFormat = Json.format[Message]
  implicit val chatParticipantJsonFormat = Json.format[ChatParticipant]
}
