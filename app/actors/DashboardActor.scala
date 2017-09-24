package actors

import akka.actor.{ActorRef, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}
import common.{EntityFieldsObject, PersistentEntity}
import org.slf4j.LoggerFactory

import scala.collection.mutable

case class Item(var name: String, var score: Int) {

  def this(name: String ) {
    this(name, 0)
  }

  def increment(): Unit = this.score = this.score + 1

  def decrement(): Unit = if (this.score > 0) this.score = this.score - 1


}

case class Dashboard(id: String,
                      name: String,
                      description: String,
                      style: String,
                      items: mutable.Map[String, Item] = mutable.Map(),
                     ownerName: String,
                     ownerEmail: String,
                     readonlyHash: String,
                     writeHash: String,
                    deleted: Boolean = false
                    ) extends EntityFieldsObject[String, Dashboard] {
  /**
    * Assigns an id to the fields object, returning a new instance
    *
    * @param id The id to assign
    */
  override def assignId(id: String) = this.copy(id = id)
  override def markDeleted = this.copy(deleted = false)
}

object DashboardActor {

  private val LOG = LoggerFactory.getLogger(classOf[DashboardActor])

//  trait Factory {
//    def create(@Assisted("dashboard") dashboard: Dashboard): Actor
//  }

  //def props(dashboard: Dashboard): Props = Props(new DashboardActor(dashboard))

}

class DashboardActor(id: String) extends PersistentEntity[Dashboard](id) {

  final private val watchers: Set[ActorRef] = Set();

  private def handleIncrementItemCommand(command: Dashboard.IncrementItem): Unit = {
    val item: Item = dashboard.items(command.name)
    if (item != null) {
      item.increment()
    }
    notifyWatchers()
  }

  private def handleDecremenItemCommand (command: Dashboard.DecrementItem): Unit = {
    val item: Item = dashboard.items(command.name)
    if (item != null) {
      item.decrement ()
    }
    notifyWatchers ()
  }

  private def handleWatchCommand (command: Dashboard.Watch): Unit = {
    val data: Dashboard.Data = new Dashboard.Data(dashboard.items)
    sender.tell (data, self)
    watchers.+(sender)
  }

  private def handleDataCommand (command: Dashboard.Data): Unit = {
    val data: Dashboard.Data = new Dashboard.Data (dashboard.items)
    sender.tell (data, self)
  }

  private def handleUnwatchCommand (command: Dashboard.Unwatch): Unit = {
    watchers.-(sender())
  }

  private def handleGetWriteHashCommand (command: Dashboard.GetWriteHash): Unit = {
    sender().tell (dashboard.writeHash, self)
  }

  private def handleGetReadonlyHashCommand (command: Dashboard.GetReadonlyHash): Unit = {
    sender().tell (dashboard.readonlyHash, self)
  }

  private def handleGetNameCommand (command: Dashboard.GetName): Unit = {
    sender().tell (dashboard.name, self)
  }

  private def handlerGetDashboard(command: Dashboard.GetDashboard): Unit = {
    sender().tell(dashboard, self)
  }

  private def handleAddItemCommand (command: Dashboard.AddItem): Unit = {
    val addItem: Dashboard.AddItem = command.asInstanceOf[Dashboard.AddItem]
    dashboard.items + addItem.name
    notifyWatchers ()
  }

  private def handleRemoveItemCommand (command: Dashboard.RemoveItem): Unit = {
    val removeItem: Dashboard.RemoveItem = command.asInstanceOf[Dashboard.RemoveItem]
    dashboard.items - removeItem.name
    notifyWatchers ()
  }

  private def notifyWatchers (): Unit = {
    for (watcher:ActorRef <- watchers) {
      watcher.tell(new Dashboard.Data (dashboard.items), self);
    }
  }

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, snapshot: Dashboard) =>
      dashboard = snapshot
  }

  override def receiveCommand: Receive = {
    case cmd:Dashboard.RemoveItem => handleRemoveItemCommand(cmd)
    case cmd:Dashboard.AddItem => handleAddItemCommand(cmd)
    case cmd:Dashboard.DecrementItem => handleDecremenItemCommand(cmd)
    case cmd:Dashboard.IncrementItem => handleIncrementItemCommand(cmd)
    case cmd:Dashboard.Data => handleDataCommand(cmd)
    case cmd:Dashboard.GetName => handleGetNameCommand(cmd)
    case cmd:Dashboard.GetReadonlyHash => handleGetReadonlyHashCommand(cmd)
    case cmd:Dashboard.GetWriteHash => handleGetWriteHashCommand(cmd)
    case cmd:Dashboard.GetDashboard => handlerGetDashboard(cmd)

    saveSnapshot(dashboard)
  }

  override def persistenceId: String = dashboard.writeHash
}