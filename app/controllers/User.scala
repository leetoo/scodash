package controllers

import akka.actor.{ActorRef, Props}
import play.api.libs.json._

case class UserFO(id: String)

class User(id: String, out: ActorRef) extends AbstractBaseActor {

  implicit private val ItemWrites = Json.writes[ItemFO]
  implicit private val DashoboardWrites = Json.writes[DashboardFO]

  override def receive = {
//    case addItem: Dashboard.Command.AddItem =>
//      val message = Json.newObject.put("type", "additem").put("name", addItem.name)
//      out ! message
//    case removeItem: Dashboard.Command.RemoveItem =>
//      val message = Json.newObject.put("type", "removeitem").put("name", removeItem.name)
//      out ! message
    case dashboard: DashboardFO =>
      out ! Json.toJson(dashboard)
    case jsObj: JsObject =>
      jsObj.value("operation") match {
        case JsString("increment") => {
          log.info("increment:")
          log.info(jsObj.toString())
        }
        case JsString("decrement") =>
          log.info("decrement")
      }

    case cmd:Any =>
      log.warning("Unexpected user command {}", cmd)


  }
}

object User {
  val Name = "user"
  def props(id: String, out: ActorRef) = Props(classOf[User], id, out)

}

