package controllers

import java.util.UUID

import Dashboard.Command.CreateDashboard
import Scodash.{CreateNewDashboard, FindDashboard}
import akka.actor.Props
import controllers.PersistentEntity.GetState
import controllers.Scodash.Command.{CreateNewDashboard, FindDashboard}
import org.apache.commons.lang3.RandomStringUtils

import scala.collection.mutable

object Scodash {
  object Command {
    case class FindDashboardByWriteHash(writeHash: String) extends EntityCommand {
      override def entityId: String = writeHash
    }
    case class CreateNewDashboard(name: String, description: String, style: String, items: mutable.Map[String, ItemFO] = mutable.Map(), ownerName: String, ownerEmail: String)
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
  }

  def entityProps(id: String) = Dashboard.props(id)
}