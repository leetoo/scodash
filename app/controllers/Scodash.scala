package controllers

import java.util.UUID

import akka.actor.{ActorRef, Props}
import controllers.Dashboard.Command.CreateDashboard
import controllers.PersistentEntity.GetState
import controllers.Scodash.Command.{CreateNewDashboard, CreateUser, FindDashboard}
import org.apache.commons.lang3.RandomStringUtils

import scala.collection.mutable

object Scodash {
  object Command {
    case class FindDashboard(id: String)
    case class CreateNewDashboard(name: String, description: String, style: String, items: Map[String, ItemFO] = Map(), ownerName: String, ownerEmail: String)
    case class CreateUser(id: String, webOutActor: ActorRef)
  }


  def props = Props[Scodash]

  val Name = "scodash"
}

class Scodash extends Aggregate[DashboardFO, Dashboard] {

  override def receive = {
    case FindDashboard(id) =>
      log.info("Finding dashboard {}", id)
      val dashboard = lookupOrCreateChild(id)
      forwardCommand(id, GetState)

    case CreateNewDashboard(name, description, style, items, ownerName, ownerEmail) =>
      log.info("Creating new dashboard with name {}", name)
      val id = UUID.randomUUID().toString
      val readonlyHash = RandomStringUtils.randomAlphanumeric(8)
      val writeHash = RandomStringUtils.randomAlphanumeric(8)
      val fo = DashboardFO(id, name, description, style, items, ownerName, ownerEmail, readonlyHash, writeHash)
      val command = CreateDashboard(fo)
      forwardCommand(id, command)

    case CreateUser(id, webOutActor) =>
      val user = context.actorOf(User.props(id), User.Name)
      sender ! user
  }

  def entityProps(id: String) = Dashboard.props(id)
}