import java.sql.Timestamp
import java.text.SimpleDateFormat
import play.api.Play.current
import play.api.libs.json._

package object models {
  val DB = play.api.db.slick.DB
  val mysql = scala.slick.driver.MySQLDriver.simple
  val sql = scala.slick.jdbc.StaticQuery

  implicit object timestampFormat extends Format[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
    def reads(json: JsValue) = {
      val str = json.as[String]
      println(str)

      JsSuccess(new Timestamp(format.parse(str).getTime))
    }
    def writes(ts: Timestamp) = JsString(format.format(ts))
  }

  implicit val personJsonFormat = Json.format[Person]
  implicit val roomJsonFormat = Json.format[Room]
  implicit val messageJsonFormat = Json.format[Message]
  implicit val chatParticipantJsonFormat = Json.format[ChatParticipant]
}
