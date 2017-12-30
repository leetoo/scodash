package controllers.actors

import akka.actor.{ActorRef, Props}
import akka.util.Timeout
import controllers._
import org.json4s.DefaultFormats
import org.json4s.ext.JodaTimeSerializers
import play.api.libs.json.{JsString, _}

import scala.concurrent.duration._

case class UserFO(id: String)

class User (id: String, outActor: ActorRef, dashboardActor: ActorRef) extends AbstractBaseActor {

  implicit private val ItemWrites = Json.writes[ItemFO]
  implicit private val DashoboardWrites = Json.writes[DashboardFO]

  implicit val timeout: Timeout = 5.seconds
  implicit lazy val formats = DefaultFormats ++ JodaTimeSerializers.all

  override def receive = {
//    case addItem: Dashboard.Command.AddItem =>
//      val message = Json.newObject.put("type", "additem").put("name", addItem.name)
//      out ! message
//    case removeItem: Dashboard.Command.RemoveItem =>
//      val message = Json.newObject.put("type", "removeitem").put("name", removeItem.name)
//      out ! message
    case dashboard: DashboardFO =>
      outActor ! Json.toJson(dashboard)
    case jsObj: JsObject =>
      val hash = jsObj.value("hash").toString()
      val itemId = jsObj.value("itemId").toString()
      jsObj.value("operation") match {
        case JsString("increment") =>
          log.info("Increment item {} of dashboard {}", itemId, hash)
          sendCmdToDashboard(hash, Dashboard.Command.IncrementItem(itemId))
        case JsString("decrement") =>
          log.info("Decrement item {} of dashboard {}", itemId, hash)
          sendCmdToDashboard(hash, Dashboard.Command.DecrementItem(itemId))
        case _ =>
          log.warning("Unexpected user command {}", jsObj)
      }
    case cmd:Any =>
      log.warning("Unexpected user command {}", cmd)


  }

  private def sendCmdToDashboard(hash: String, cmd: Any) = {
//    (scodashActor ? Scodash.Command.FindDashboardByWriteHash(hash)).mapTo[FullResult[List[JObject]]].map {
//      result => {
//        val dashboardFO = result.value.head.extract[DashboardFO]
//        (scodashActor ? Scodash.Command.FindDashboard(dashboardFO.id)).mapTo[ActorRef].map {
//          dasboardActor => dasboardActor ! cmd
//        }
//      }
//    }
    dashboardActor ! cmd
  }
}

object User {
  val Name = "user"
  def props(id: String, outActor: ActorRef, dashboardActor: ActorRef) = Props(classOf[User], id, outActor, dashboardActor)

}

