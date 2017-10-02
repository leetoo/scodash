package controllers

import akka.actor.Props

case class UserFO(id: String)

class User(id: String) extends AbstractBaseActor {
  override def receive = ???
}

object User {
  val Name = "user"
  def props(id: String) = Props(classOf[User], id)

}

