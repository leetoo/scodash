package controllers

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._
import play.api.libs.functional.syntax._

trait JsonSupport {

  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  implicit val jodaDateReads = Reads[DateTime](js =>
    js.validate[String].map[DateTime](dtString =>
      DateTime.parse(dtString, DateTimeFormat.forPattern(dateFormat))
    )
  )
  implicit val jodaDateWrites: Writes[DateTime] = (d: DateTime) => JsString(d.toString())
  implicit private val ItemWrites = Json.writes[ItemFO]
  implicit private val DashoboardWrites = Json.writes[DashboardFO]

  // forms

  implicit val newDashboardReads: Reads[Forms.NewDashboard] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "description").read[String]
    )(Forms.NewDashboard.apply _)



  implicit val residentReads: Reads[Forms.CreateDashboardItems] = (
    (JsPath \ "items") read[Set[String]]
    )(Forms.CreateDashboardItems.apply _)
}
