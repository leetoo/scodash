package controllers

import akka.actor.{ActorRef, Props}
import com.fasterxml.jackson.databind.node.ObjectNode
import play.libs.Json

case class UserFO(id: String)

class User(id: String, out: ActorRef) extends AbstractBaseActor {
  override def receive = {
    case addItem: Dashboard.Command.AddItem =>
      val message = Json.newObject.put("type", "additem").put("name", addItem.name)
      out ! message
    case removeItem: Dashboard.Command.RemoveItem =>
      val message = Json.newObject.put("type", "removeitem").put("name", removeItem.name)
      out ! message
    case cmd:Any =>
      log.warning("Unexpected user command {}", cmd)


  }
}

object User {
  val Name = "user"
  def props(id: String, out: ActorRef) = Props(classOf[User], id, out)

}

