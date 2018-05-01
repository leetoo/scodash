package controllers.actors

import java.util.UUID

import akka.actor.{ActorRef, Props}
import akka.util.Timeout
import controllers.Dashboard.Command.CreateDashboard
import controllers.PersistentEntity.GetState
import controllers._
import controllers.actors.Scodash.Command.{CreateDashboardUser, CreateNewDashboard, FindDashboard}
import org.apache.commons.lang3.RandomStringUtils
import org.joda.time.{DateTime, DateTimeZone, LocalDate}

import scala.concurrent.duration._

object Scodash {
  object Command {
    case class FindDashboard(id: String)
    case class FindDashboardByWriteHash(hash: String)
    case class FindDashboardByReadHash(hash: String)
    case class CreateNewDashboard(name: String, description: String, items: Set[ItemFO] = Set(), ownerName: String, ownerEmail: String, timeZone: DateTimeZone)
    case class CreateDashboardUser(userId: String, webOutActor: ActorRef, dashboardId: String, mode: DashboardAccessMode.Value)
  }


  def props = Props[Scodash]

  final val Name = "scodash"

}

class Scodash extends Aggregate[DashboardFO, Dashboard] {

  implicit val timeout: Timeout = 5.seconds

  override def receive = {
    case FindDashboard(id) =>
      log.info("Finding dashboard {}", id)
      val dashboard = lookupOrCreateChild(id)
      forwardCommand(id, GetState)

    case CreateNewDashboard(name, description, items, ownerName, ownerEmail, dateTimeZone) =>
      log.info("Creating new dashboard with name {}", name)
      val id = UUID.randomUUID().toString
      val readonlyHash = RandomStringUtils.randomAlphanumeric(8)
      val writeHash = RandomStringUtils.randomAlphanumeric(8)
      val fo = DashboardFO(id, name, description, List() ++ items, ownerName, ownerEmail, readonlyHash, writeHash, DateTime.now(dateTimeZone), DateTime.now(dateTimeZone))
      val command = CreateDashboard(fo)
      forwardCommand(id, command)

    case CreateDashboardUser(id, webOutActor, dashboardId, mode) =>
      val dashboardActor = lookupOrCreateChild(dashboardId)
      val user = context.actorOf(User.props(id, webOutActor, dashboardActor, mode), id)
      forwardCommand(dashboardId, Dashboard.Command.Watch(user))
      sender ! user

  }

  def entityProps(id: String) = Dashboard.props(id)
}