package controllers.actors


import akka.actor.{ActorRef, Props}
import akka.util.Timeout
import controllers.{AbstractBaseActor, Dashboard, DashboardFO, ItemFO}
import org.json4s.DefaultFormats
import org.json4s.ext.JodaTimeSerializers
import play.api.libs.json.{JsString, _}

import scala.concurrent.duration._

case class UserFO(id: String)

class User (id: String, outActor: ActorRef, dashboardActor: ActorRef, mode: DashboardAccessMode.Value) extends AbstractBaseActor {

  implicit private val ItemWrites = Json.writes[ItemFO]
  implicit private val DashoboardWrites = Json.writes[DashboardFO]

  implicit val timeout: Timeout = 5.seconds
  implicit lazy val formats = DefaultFormats ++ JodaTimeSerializers.all

  private var sorting: DashboardSorting.Value = DashboardSorting.AZ

  override def receive = {
    case dashboard: DashboardFO =>
      var dashUpdated = (mode match {
        case DashboardAccessMode.READONLY => dashboard.removeWriteHash
        case DashboardAccessMode.WRITE => dashboard.removeReadOnlyHash
      })
      dashUpdated = sorting match {
        case DashboardSorting.SCORE => dashUpdated.sortByScore
        case DashboardSorting.AZ => dashUpdated.sortByAZ
      }
      outActor ! Json.toJson(dashUpdated)
    case jsObj: JsObject =>
      val hash = jsObj.value("hash").asInstanceOf[JsString].value
      jsObj.value("operation") match {
        case JsString("increment") =>
          val itemId = jsObj.value("itemId").toString()
          log.info("Increment item {} of dashboard {}", itemId, hash)
          sendCmdToDashboard(hash, Dashboard.Command.IncrementItem(itemId, hash))
        case JsString("decrement") =>
          val itemId = jsObj.value("itemId").toString()
          log.info("Decrement item {} of dashboard {}", itemId, hash)
          sendCmdToDashboard(hash, Dashboard.Command.DecrementItem(itemId, hash))
        case JsString("sort") =>
          val newSorting = jsObj.value("sorting").toString()
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
}

object User {
  val Name = "user"
  def props(id: String, outActor: ActorRef, dashboardActor: ActorRef, mode: DashboardAccessMode.Value) = Props(classOf[User], id, outActor, dashboardActor, mode)

}

