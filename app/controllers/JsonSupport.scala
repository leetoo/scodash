package controllers

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.functional.syntax._
import play.api.libs.json._

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
    ) (Forms.NewDashboard.apply _)

  implicit val createDashboardItemsReads: Reads[Forms.CreateDashboardItems] =
    (JsPath \ "items").read[Set[String]]
  Forms.CreateDashboardItems.apply _

  implicit val createDashboardReads: Reads[Forms.CreatedDashboard] =
    (JsPath \ "name").read[String] and
    (JsPath \ "writeHash").read[String] and
    (JsPath \ "readOnlyHash").read[String]
  Forms.CreatedDashboard.apply _

  implicit val dashboardReads: Reads[Forms.Dashboard] =
    (JsPath \ "name").read[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "items").read[Set[String]] and
      (JsPath \ "ownerName").read[String] and
      (JsPath \ "ownerEmail").read[String]
  Forms.Dashboard.apply _

  implicit val dashboardWrites: Writes[Forms.Dashboard] =
    (JsPath \ "name").write[String] and
      (JsPath \ "description").write[String] and
      (JsPath \ "items").write[Set[String]] and
      (JsPath \ "ownerName").write[String] and
      (JsPath \ "ownerEmail").write[String]
  unlift(Forms.Dashboard.unapply)

  // FO classes
  implicit val dashboardFoWrites: Writes[DashboardFO] =
    (JsPath \ "id").write[String] and
      (JsPath \ "name").write[String] and
      (JsPath \ "description").write[String] and
      (JsPath \ "items").write[Set[String]] and
      (JsPath \ "ownerName").write[String] and
      (JsPath \ "ownerEmail").write[String] and
      (JsPath \ "readonlyHash").write[String] and
      (JsPath \ "writeHash").write[String] and
      (JsPath \ "created").write[DateTime] and
      (JsPath \ "deleted").write[DateTime]
  unlift(Forms.DashboardFo.unapply)

  implicit val dashboardFoReads: Reads[DashboardFO] =
    (JsPath \ "id").write[String] and
      (JsPath \ "name").write[String] and
      (JsPath \ "description").write[String] and
      (JsPath \ "items").write[Set[String]] and
      (JsPath \ "ownerName").write[String] and
      (JsPath \ "ownerEmail").write[String] and
      (JsPath \ "readonlyHash").write[String] and
      (JsPath \ "writeHash").write[String] and
      (JsPath \ "created").write[DateTime] and
      (JsPath \ "deleted").write[DateTime]
  DashboardFo.apply _



}
