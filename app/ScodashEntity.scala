import ScodashEntity.{ScodashEvent, ScodashState}
import akka.persistence.{PersistentActor, SnapshotOffer}
import pojo.Dashboard.Cmd
import pojo.{Dashboard, DashboardId, Item}

import scala.collection.mutable

class ScodashEntity extends PersistentActor {

  private var state = ScodashState()

  override def receiveRecover: Receive = {
    case event: ScodashEvent =>
      state += event
    case SnapshotOffer(_, snapshot: ScodashState) =>
      state = snapshot
  }

  override def receiveCommand: Receive = ???

  override def persistenceId: String = "scodash"


}

object ScodashEntity {

  sealed trait ScodashCommand

  case class Watch(id: DashboardId) extends ScodashCommand
  case class Unwatch(id: DashboardId) extends ScodashCommand
  case class Update(id: DashboardId, items: mutable.Map[String, Item]) extends ScodashCommand
  case class IncrementItem(id: DashboardId, name: String) extends ScodashCommand
  case class DecrementItem(id: DashboardId, name: String) extends ScodashCommand
  case class AddItem(id: DashboardId, name: String) extends ScodashCommand
  case class RemoveItem(id: DashboardId, name: String) extends ScodashCommand
  case class GetWriteHash(id: DashboardId) extends ScodashCommand
  case class GetReadonlyHash(id: DashboardId) extends ScodashCommand
  case class GetName(id: DashboardId) extends ScodashCommand
  case class GetDashboard(id: DashboardId) extends ScodashCommand

  sealed trait ScodashEvent {
    val id: DashboardId
    val dashboard: Dashboard
  }

  final case class DashboardNotFound(id: DashboardId) extends RuntimeException(s"Blog post not found with id $id")

  type MaybeDashbord[+A] = Either[DashboardNotFound, A]

  final case class ScodashState(dashboards: Map[DashboardId, Dashboard]) {
    def apply(id: DashboardId): MaybeDashbord[Dashboard] = dashboards.get(id).toRight(DashboardNotFound(id))
    def +(event: ScodashEvent): ScodashState = ScodashState(dashboards.updated(event.id, event.dashboard))
  }

  object ScodashState {
    def apply(): ScodashState = ScodashState(Map.empty)
  }
}
