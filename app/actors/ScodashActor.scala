package actors

import akka.actor.Props
import akka.pattern.pipe

import actors.ScodashActor._
import akka.actor.ActorRef
import akka.persistence.{PersistentActor, SnapshotOffer}
import pojo.{Dashboard, DashboardId}

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
    case GetDashboard(id) =>
      sender() ! state(id)
    case CreateDashboard(dashboard) =>
      val dashboardActor = context.actorOf(DashboardActor.props(dashboard))
      handleEvent(DashboardCreated(DashboardId(dashboard.writeHash), dashboardActor)) pipeTo sender
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
  case class GetDashboard(id: DashboardId) extends ScodashCommand

  sealed trait ScodashEvent {
    val id: DashboardId
    val dashboardActor: ActorRef
  }

  case class DashboardCreated(id: DashboardId, dashboardActor: ActorRef) extends ScodashEvent
  final case class DashboardNotFound(id: DashboardId) extends RuntimeException(s"Blog post not found with id $id")

  type MaybeDashbord[+A] = Either[DashboardNotFound, A]

  final case class ScodashState(dashboardActors: Map[DashboardId, ActorRef]) {
    //def apply(id: DashboardId): MaybeDashbord[ActorRef] = dashboardActors.get(id).toRight(DashboardNotFound(id))
    def apply(id: DashboardId): ActorRef = dashboardActors.get(id).get
    def +(event: ScodashEvent): ScodashState = ScodashState(dashboardActors.updated(event.id, event.dashboardActor))
  }

  object ScodashState {
    def apply(): ScodashState = ScodashState(Map.empty)
  }
}

