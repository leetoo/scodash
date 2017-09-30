package controllers

import akka.actor.Props
import controllers.User.Command.CreateUser
import controllers.User.Event.UserCreated

case class UserFO(id: String)

class User(id: String) extends PersistentEntity[UserFO](id) {

  override def additionalCommandHandling: Receive = {
    case CreateUser(user) =>
      persist(UserCreated(user))(handleEventAndRespond())
  }

  override def isCreateMessage(cmd: Any) = cmd match {
    case cr:CreateUser => true
    case _ => false
  }

  def handleEvent(event:EntityEvent):Unit = event match {
    case UserCreated(dashboard) =>
      state = dashboard
  }

}

object User {

  val EntityType = "user"

  object Command {
    case class CreateUser(user: UserFO)
  }

  object Event {
    trait UserEvent extends EntityEvent{override def entityType: String = EntityType}
    case class UserCreated(user: UserFO) extends UserEvent
  }

  def props(id: String) = Props(classOf[User], id)


}

