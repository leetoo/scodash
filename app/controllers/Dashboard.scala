package controllers

import akka.actor.{ActorRef, Props}
import controllers.Dashboard.Command._
import controllers.Dashboard.Event.{DashboardCreated, DashboardUpdated}

import scala.collection.mutable

case class ItemFO(id: Int, name: String, var score: Int = 0) {
  def increment(): Unit = score = score + 1
  def decrement(): Unit = if (score > 0) score = score - 1
}

object DashboardFO {
  def empty = DashboardFO("", "", "", "", List.empty[ItemFO], "", "", "", "", -1, -1)

}

@SerialVersionUID(1L)
case class DashboardFO(id: String, name: String, description: String, style: String, items: List[ItemFO] = List[ItemFO](),
                       ownerName: String, ownerEmail: String, readonlyHash: String, writeHash: String,
                       created:Long, updated:Long, deleted: Boolean = false) extends EntityFieldsObject[String, DashboardFO] {
  override def assignId(id: String) = this.copy(id = id)
  override def markDeleted = this.copy(deleted = false)
  def removeReadOnlyHash = this.copy(readonlyHash = "")
  def removeWriteHash = this.copy(writeHash = "")
  def updatedNow = this.copy(updated = System.currentTimeMillis())
  def sortByScore = this.copy(items = List() ++ (items.sortWith((it1, it2) => it1.score > it2.score)))
  def sortByAZ = this.copy(items = List() ++ (items.sortWith((it1, it2) => it1.name < it2.name)))
}

class Dashboard(id: String) extends PersistentEntity[DashboardFO](id) {

  import Dashboard._

  def initialState = DashboardFO.empty
  final val watchers: mutable.Set[ActorRef] = mutable.Set();


  override def additionalCommandHandling: Receive = {
    case CreateDashboard(dashboard) =>
      // don't allow if not in initial state
      if (state != initialState) {
        sender() ! Failure(FailureType.Validation, DashboardAlreadyCreated)
      } else {
        persist(DashboardCreated(dashboard))(handleEventAndRespond())
      }
    case Watch(watcher) =>
      watchers += watcher
      watcher ! state
    case Unwatch(watcher) =>
      watchers -= watcher
    case IncrementItem(id, hash) =>
      if (hash == state.writeHash) {
        state = state.updatedNow
        state.items.find(item => item.id.toString == id).map { item =>
          item.increment()
          persist(DashboardUpdated(state))(handleEventAndRespond())
          watchers.foreach(w => w ! state)
        }
      }
    case DecrementItem(id, hash) =>
      if (hash == state.writeHash) {
        state = state.updatedNow
        state.items.find(item => item.id.toString == id).map { item =>
          item.decrement()
          persist(DashboardUpdated(state))(handleEventAndRespond())
          watchers.foreach(w => w ! state)
        }
      }
    case SortingChanged() =>
      sender() ! state

  }

  override def isCreateMessage(cmd: Any) = cmd match {
    case cr:CreateDashboard => true
    case _ => false
  }

  def handleEvent(event:EntityEvent):Unit = event match {
    case DashboardCreated(dashboard) =>
      state = dashboard
    case DashboardUpdated(dashboard) =>
      state = dashboard
  }

}

object Dashboard {

  val EntityType = "dashboard"

  object Command {
    case class CreateDashboard(dashboard: DashboardFO)
    case class Watch(watcher: ActorRef)
    case class Unwatch(watcher: ActorRef)
    case class IncrementItem(id: String, hash: String)
    case class DecrementItem(id: String, hash: String)
    case class SortingChanged()
  }

  object Event {
    trait DasboardEvent extends EntityEvent{override def entityType: String = EntityType}
    case class DashboardCreated(dashboard: DashboardFO) extends DasboardEvent
    case class DashboardUpdated(dashboard: DashboardFO) extends DasboardEvent
  }



  def props(id: String) = Props(classOf[Dashboard], id)

  val DashboardAlreadyCreated = ErrorMessage("dashboard.alreadyexists", Some("This dashboard has already been created and can not handle another CreateDashboard request"))

}

