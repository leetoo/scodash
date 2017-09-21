package actors

import java.util.concurrent.TimeUnit

import akka.pattern.pipe
import akka.persistence.{PersistentActor, SnapshotOffer}
import akka.util.Timeout
import controllers.Application
import pojo.{Dashboard, DashboardId}

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{Future, Promise}

class ScodashActor extends PersistentActor {

  import ScodashActor._
  import context._

  private var state = ScodashState()

  override def receiveRecover: Receive = {
    case event: ScodashEvent =>
      state += event
    case SnapshotOffer(_, snapshot: ScodashState) =>
      state = snapshot
  }

  override def receiveCommand: Receive = {
    case GetDashboardActor(id) =>
      context.actorOf(DashboardActor.props(Dashboard(writeHash = id.writeHash)))
      context.actorSelection(state(id)).resolveOne(FiniteDuration(10, TimeUnit.SECONDS)) pipeTo sender
    case CreateDashboard(dashboard) =>
      val dashboardActorName = s"dashboard-${dashboard.writeHash}"
      val dashboardActor = context.actorOf(DashboardActor.props(dashboard), dashboardActorName)
      handleEvent(DashboardCreated(DashboardId(dashboard.writeHash), dashboardActorName)) pipeTo sender
      ()
  }

  private def handleEvent[E <: ScodashEvent](e: => E): Future[E] = {
    val p = Promise[E]
    persist(e) { event =>
      p.success(event)
      state += event
      context.system.eventStream.publish(event)
      if (lastSequenceNr != 0 && lastSequenceNr % 1000 == 0) saveSnapshot(state)
    }
    p.future
  }



  override def persistenceId: String = "scodash"


}

object ScodashActor {

  sealed trait ScodashCommand

  case class CreateDashboard(dashboard: Dashboard) extends ScodashCommand
  case class GetDashboardActor(id: DashboardId) extends ScodashCommand

  sealed trait ScodashEvent {
    val id: DashboardId
    val dashboardActorName: String
  }

  case class DashboardCreated(id: DashboardId, dashboardActorName: String) extends ScodashEvent
  final case class DashboardNotFound(id: DashboardId) extends RuntimeException(s"Blog post not found with id $id")

  type MaybeDashbord[+A] = Either[DashboardNotFound, A]

  final case class ScodashState(dashboardActorNames: Map[DashboardId, String]) {
    //def apply(id: DashboardId): MaybeDashbord[ActorRef] = dashboardActors.get(id).toRight(DashboardNotFound(id))
    def apply(id: DashboardId): String = dashboardActorNames.get(id).get
    def +(event: ScodashEvent): ScodashState = ScodashState(dashboardActorNames.updated(event.id, event.dashboardActorName))
  }

  object ScodashState {
    def apply(): ScodashState = ScodashState(Map.empty)
  }
}

