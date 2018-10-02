package controllers.actors

import java.util.UUID

import akka.actor.{ActorRef, Props}
import akka.util.Timeout
import controllers.Dashboard.Command.CreateDashboard
import controllers.PersistentEntity.GetState
import controllers._
import controllers.actors.Scodash.Command._
import org.apache.commons.lang3.RandomStringUtils
import org.joda.time.{DateTime, DateTimeZone}

import scala.collection.mutable
import scala.concurrent.duration._

object Scodash {
  object Command {
    case class FindDashboard(id: String)
    case class FindDashboardByWriteHash(hash: String)
    case class FindDashboardByReadonlyHash(hash: String)
    case class CreateNewDashboard(name: String, description: String, items: Set[ItemFO] = Set(), ownerName: String, ownerEmail: String, timeZone: DateTimeZone)
    case class CreateDashboardUser(userId: String, webOutActor: ActorRef, dashboardId: String, mode: DashboardAccessMode.Value)
  }

  def props = Props[Scodash]

  final val Name = "scodash"

}

class Scodash extends Aggregate[DashboardFO, Dashboard] {

  implicit val timeout: Timeout = 5.seconds

  val readOnlyIds: mutable.Map[String, String] = mutable.Map()
  val writeIds: mutable.Map[String, String] = mutable.Map()

  override def receive = {
    case FindDashboard(id) =>
      log.info("Finding dashboard {}", id)
      val dashboard = lookupOrCreateChild(id)
      forwardCommand(id, GetState)

    case FindDashboardByReadonlyHash(hash) =>
      log.info("Finding dashboard by read hash {}", hash)
      readOnlyIds.get(hash) match {
        case Some(id) =>
          log.info("Found in readOnly map {}", hash)
          val dashboard = lookupOrCreateChild(id)
          forwardCommand(id, GetState)
        case None => sender ! None
      }

    case FindDashboardByWriteHash(hash) =>
      log.info("Finding dashboard by write hash {}", hash)
      writeIds.get(hash) match {
        case Some(id) =>
          log.info("Found in write map {}", hash)
          val dashboard = lookupOrCreateChild(id)
          forwardCommand(id, GetState)
        case None => sender ! None
      }

    case CreateNewDashboard(name, description, items, ownerName, ownerEmail, dateTimeZone) =>

      log.info("Creating new dashboard with name {}", name)

      val id = UUID.randomUUID().toString
      val readonlyHash = RandomStringUtils.randomAlphanumeric(8)
      val writeHash = RandomStringUtils.randomAlphanumeric(8)

      readOnlyIds(readonlyHash) = id
      writeIds(writeHash) = id

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