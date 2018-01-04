package controllers.actors

import java.time.{LocalDateTime, ZonedDateTime}
import java.util.{Date, UUID}

import akka.actor.{ActorRef, Props}
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import controllers.Dashboard.Command.CreateDashboard
import controllers.PersistentEntity.GetState
import controllers._
import controllers.actors.Scodash.Command.{CreateDashboardUser, CreateNewDashboard, FindDashboard}
import org.apache.commons.lang3.RandomStringUtils
import org.joda.time.DateTime

import scala.concurrent.duration._

object Scodash {
  object Command {
    case class FindDashboard(id: String)
    case class FindDashboardByWriteHash(hash: String)
    case class FindDashboardByReadHash(hash: String)
    case class CreateNewDashboard(name: String, description: String, style: String, items: Set[ItemFO] = Set(), ownerName: String, ownerEmail: String)
    case class CreateDashboardUser(userId: String, webOutActor: ActorRef, dashboardId: String, mode: DashboardAccessMode)
  }


  def props = Props[Scodash]

  final val Name = "scodash"

}

class Scodash extends Aggregate[DashboardFO, Dashboard] {

  import context.dispatcher

  implicit val timeout: Timeout = 5.seconds

  val projection = ResumableProjection("scodash", context.system)
  implicit val mater = ActorMaterializer()
  val journal: JdbcReadJournal = PersistenceQuery(context.system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)
  projection.fetchLatestOffset.foreach{ o =>
    journal.
      eventsByTag("dashboardcreated", o.getOrElse(0L)).
      runForeach(e => self ! e)
  }

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
      val fo = DashboardFO(id, name, description, style, items, ownerName, ownerEmail, readonlyHash, writeHash, System.currentTimeMillis(), System.currentTimeMillis())
      val command = CreateDashboard(fo)
      forwardCommand(id, command)

    case CreateDashboardUser(id, webOutActor, dashboardId, mode) =>
      val dashboardActor = lookupOrCreateChild(dashboardId)
      val user = context.actorOf(User.props(id, webOutActor, dashboardActor, mode), id)
      forwardCommand(dashboardId, Dashboard.Command.Watch(user))
      sender ! user

    case EventEnvelope(offset, pid, seq, event) =>
      projection.storeLatestOffset(offset)
  }

  def entityProps(id: String) = Dashboard.props(id)
}