import pojo.Dashboard.Cmd
import pojo.{DashboardId, Item}

import scala.collection.mutable

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
}
