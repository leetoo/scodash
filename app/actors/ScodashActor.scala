package actors

import java.util.UUID

import actors.Dashboard.Command.CreateDashboard
import actors.Scodash.{CreateNewDashboard, FindDashboard}
import common.Aggregate
import common.PersistentEntity.GetState
import org.apache.commons.lang3.RandomStringUtils

import scala.collection.mutable

object Scodash {
  case class FindDashboard(id: String)
  case class CreateNewDashboard(name: String, description: String, style: String, items: mutable.Map[String, ItemFO] = mutable.Map(), ownerName: String, ownerEmail: String)
}

class Scodash extends Aggregate[DashboardFO, Dashboard] {
  /**
    * Gets the Props needed to create the child entity for this factory
    *
    * @return a Props instance
    */
  override def entityProps(id: String) = ???

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
}