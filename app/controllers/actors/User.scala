package controllers.actors


import akka.actor.{ActorRef, Props}
import akka.util.Timeout
import controllers.{AbstractBaseActor, Dashboard, DashboardFO, ItemFO}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

case class UserFO(id: String)

class User (id: String, outActor: ActorRef, dashboardActor: ActorRef, mode: DashboardAccessMode.Value) extends AbstractBaseActor {

  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  implicit val jodaDateReads = Reads[DateTime](js =>
    js.validate[String].map[DateTime](dtString =>
      DateTime.parse(dtString, DateTimeFormat.forPattern(dateFormat))
    )
  )
  implicit val jodaDateWrites: Writes[DateTime] = new Writes[DateTime] {
    def writes(d: DateTime): JsValue = JsString(d.toString())
  }
  implicit private val ItemWrites = Json.writes[ItemFO]
  implicit private val DashoboardWrites = Json.writes[DashboardFO]


  implicit val timeout: Timeout = 5.seconds

  private var sorting: DashboardSorting.Value = DashboardSorting.AZ

  private var dashboardToDeliver: JsValue = JsObject.empty

  private val ticking =
    context.system.scheduler.schedule(
      20 seconds,
      20 seconds,
      outActor,
      dashboardToDeliver
    )

  override def receive = {
    case dashboard: DashboardFO =>
      var dashboardUpdated = (mode match {
        case DashboardAccessMode.READONLY => dashboard.removeWriteHash
        case DashboardAccessMode.WRITE => dashboard.removeReadOnlyHash
      })
      dashboardUpdated = sorting match {
        case DashboardSorting.SCORE => dashboardUpdated.sortByScore
        case DashboardSorting.AZ => dashboardUpdated.sortByAZ
      }
      dashboardToDeliver = Json.toJson(dashboardUpdated)
      outActor ! dashboardToDeliver
    case jsObj: JsObject =>
      val hash = jsObj.value("hash").asInstanceOf[JsString].value
      val tzOffset = jsObj.value("tzOffset").asInstanceOf[JsNumber].value
      jsObj.value("operation") match {
        case JsString("increment") =>
          val itemId = jsObj.value("itemId").toString()
          log.info("Increment item {} of dashboard {}", itemId, hash)
          sendCmdToDashboard(hash, Dashboard.Command.IncrementItem(itemId, hash, tzOffset))
        case JsString("decrement") =>
          val itemId = jsObj.value("itemId").toString()
          log.info("Decrement item {} of dashboard {}", itemId, hash)
          sendCmdToDashboard(hash, Dashboard.Command.DecrementItem(itemId, hash, tzOffset))
        case JsString("sort") =>
          val newSorting = jsObj.value("sorting").asInstanceOf[JsString].value.toUpperCase
          log.info("Sort dashboard {} by {}", hash, newSorting)
          sorting = DashboardSorting.values.find(_.toString == newSorting).getOrElse(DashboardSorting.AZ)
          sendCmdToDashboard(hash, Dashboard.Command.SortingChanged())
        case _ =>
          log.warning("Unexpected user command {}", jsObj)
      }
    case cmd:Any =>
      log.warning("Unexpected user command {}", cmd)


  }

  private def sendCmdToDashboard(hash: String, cmd: Any) = {
    dashboardActor ! cmd
  }

  override def postStop(): Unit = {
    ticking.cancel()
    super.postStop()
  }
}

object User {
  val Name = "user"
  def props(id: String, outActor: ActorRef, dashboardActor: ActorRef, mode: DashboardAccessMode.Value) = Props(classOf[User], id, outActor, dashboardActor, mode)

}

