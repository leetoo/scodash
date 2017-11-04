package controllers

import akka.actor.{ActorRef, Props}
import controllers.Dashboard.Command._
import controllers.Dashboard.Event.{DashboardCreated, ItemUpdated}

import scala.collection.mutable

case class ItemFO(id: Int, name: String, var score: Int = 0) {
  def increment(): Unit = score = score + 1
  def decrement(): Unit = if (score > 0) score = score - 1
}

object DashboardFO {
  def empty = DashboardFO("", "", "", "", Set.empty[ItemFO], "", "", "", "")
}

case class DashboardFO(id: String, name: String, description: String, style: String, items: Set[ItemFO] = Set(), ownerName: String, ownerEmail: String, readonlyHash: String, writeHash: String,deleted: Boolean = false) extends EntityFieldsObject[String, DashboardFO] {
  override def assignId(id: String) = this.copy(id = id)
  override def markDeleted = this.copy(deleted = false)
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
    case IncrementItem(id) =>
      state.items.find(item => item.id.toString == id).map { item =>
        item.increment()
        persist(ItemUpdated(id, item.score.toString))(handleEventAndRespond())
        watchers.foreach(w => w ! state)
      }
    case DecrementItem(id) =>
      state.items.find(item => item.id.toString == id).map { item =>
        item.decrement()
        persist(ItemUpdated(id, item.score.toString))(handleEventAndRespond())
        watchers.foreach(w => w ! state)
      }
  }

  override def isCreateMessage(cmd: Any) = cmd match {
    case cr:CreateDashboard => true
    case _ => false
  }

  def handleEvent(event:EntityEvent):Unit = event match {
    case DashboardCreated(dashboard) =>
      state = dashboard
    case ItemUpdated(id, score) =>
  }

//  private def handleIncrementItemCommand(command: IncrementItem): Unit = {
//    val item: ItemFO = dashboard.items(command.name)
//    if (item != null) {
//      item.increment()
//    }
//    notifyWatchers()
//  }
//
//  private def handleDecremenItemCommand (command: DecrementItem): Unit = {
//    val item: ItemFO = dashboard.items(command.name)
//    if (item != null) {
//      item.decrement ()
//    }
//    notifyWatchers ()
//  }
//
//  private def handleWatchCommand (command: Watch): Unit = {
//    val data: DashboardFO.Data = new DashboardFO.Data(dashboard.items)
//    sender.tell (data, self)
//    watchers.+(sender)
//  }
//
//  private def handleDataCommand (command: Data): Unit = {
//    val data: DashboardFO.Data = new DashboardFO.Data (dashboard.items)
//    sender.tell (data, self)
//  }
//
//  private def handleUnwatchCommand (command: Unwatch): Unit = {
//    watchers.-(sender())
//  }
//
//  private def handleGetWriteHashCommand (command: GetWriteHash): Unit = {
//    sender().tell (dashboard.writeHash, self)
//  }
//
//  private def handleGetReadonlyHashCommand (command: GetReadonlyHash): Unit = {
//    sender().tell (dashboard.readonlyHash, self)
//  }
//
//  private def handleGetNameCommand (command: GetName): Unit = {
//    sender().tell (dashboard.name, self)
//  }
//
//  private def handlerGetDashboard(command: GetDashboard): Unit = {
//    sender().tell(dashboard, self)
//  }
//
//  private def handleAddItemCommand (command: AddItem): Unit = {
//    val addItem: DashboardFO.AddItem = command.asInstanceOf[DashboardFO.AddItem]
//    dashboard.items + addItem.name
//    notifyWatchers ()
//  }
//
//  private def handleRemoveItemCommand (command: RemoveItem): Unit = {
//    val removeItem: DashboardFO.RemoveItem = command.asInstanceOf[DashboardFO.RemoveItem]
//    dashboard.items - removeItem.name
//    notifyWatchers ()
//  }
//
//  private def notifyWatchers (): Unit = {
//    for (watcher:ActorRef <- watchers) {
//      watcher.tell(new DashboardFO.Data (dashboard.items), self);
//    }
//  }
//
//  override def receiveRecover: Receive = {
//    case SnapshotOffer(_, snapshot: DashboardFO) =>
//      dashboard = snapshot
//  }
//
//  override def receiveCommand: Receive = {
//    case cmd:DashboardFO.RemoveItem => handleRemoveItemCommand(cmd)
//    case cmd:DashboardFO.AddItem => handleAddItemCommand(cmd)
//    case cmd:DashboardFO.DecrementItem => handleDecremenItemCommand(cmd)
//    case cmd:DashboardFO.IncrementItem => handleIncrementItemCommand(cmd)
//    case cmd:DashboardFO.Data => handleDataCommand(cmd)
//    case cmd:DashboardFO.GetName => handleGetNameCommand(cmd)
//    case cmd:DashboardFO.GetReadonlyHash => handleGetReadonlyHashCommand(cmd)
//    case cmd:DashboardFO.GetWriteHash => handleGetWriteHashCommand(cmd)
//    case cmd:DashboardFO.GetDashboard => handlerGetDashboard(cmd)
//
//    saveSnapshot(dashboard)
//  }
//
//  override def persistenceId: String = dashboard.writeHash



}

object Dashboard {

  val EntityType = "dashboard"

  object Command {
    case class CreateDashboard(dashboard: DashboardFO)
    case class Watch(watcher: ActorRef)
    case class Unwatch(watcher: ActorRef)
    case class IncrementItem(id: String)
    case class DecrementItem(id: String)
  }

  object Event {
    trait DasboardEvent extends EntityEvent{override def entityType: String = EntityType}
    case class DashboardCreated(dashboard: DashboardFO) extends DasboardEvent
    case class ItemUpdated(itemId: String, score: String) extends DasboardEvent
  }

  def props(id: String) = Props(classOf[Dashboard], id)

  val DashboardAlreadyCreated = ErrorMessage("dashboard.alreadyexists", Some("This dashboard has already been created and can not handle another CreateDashboard request"))

}

