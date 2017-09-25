package actors

import actors.DashboardActor.Command
import actors.DashboardActor.Command.{Data, DecrementItem, IncrementItem, Watch}
import akka.actor.{ActorRef, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}
import common.{EntityEvent, EntityFieldsObject, PersistentEntity}
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

  val EntityType = "dashboard"

//  trait Factory {
//    def create(@Assisted("dashboard") dashboard: Dashboard): Actor
//  }

  //def props(dashboard: Dashboard): Props = Props(new DashboardActor(dashboard))

  object Command {
    case class CreateDashboard(dashboard: Dashboard)
    case class Watch()
    case class Unwatch()
    case class Data(items: mutable.Map[String, Item])
    case class IncrementItem(name: String)
    case class DecrementItem(name: String)
    case class AddItem(name: String)
    case class RemoveItem(name: String)
    case class GetWriteHash()
    case class GetReadonlyHash()
    case class GetName()
    case class GetDashboard()
  }

  object Event {
    trait DasboardEvent extends EntityEvent{override def entityType: String = EntityType}
    case class DashboardCreated(dashboard: Dashboard) extends DasboardEvent {
      def toDataModel = {
        val dashboardDM = Datamodel.Dashboard.new

      }
    }
  }

 

}

class DashboardActor(id: String) extends PersistentEntity[Dashboard](id) {

  final private val watchers: Set[ActorRef] = Set();

  private def handleIncrementItemCommand(command: IncrementItem): Unit = {
    val item: Item = dashboard.items(command.name)
    if (item != null) {
      item.increment()
    }
    notifyWatchers()
  }

  private def handleDecremenItemCommand (command: DecrementItem): Unit = {
    val item: Item = dashboard.items(command.name)
    if (item != null) {
      item.decrement ()
    }
    notifyWatchers ()
  }

  private def handleWatchCommand (command: Watch): Unit = {
    val data: Dashboard.Data = new Dashboard.Data(dashboard.items)
    sender.tell (data, self)
    watchers.+(sender)
  }

  private def handleDataCommand (command: Data): Unit = {
    val data: Dashboard.Data = new Dashboard.Data (dashboard.items)
    sender.tell (data, self)
  }

  private def handleUnwatchCommand (command: Unwatch): Unit = {
    watchers.-(sender())
  }

  private def handleGetWriteHashCommand (command: GetWriteHash): Unit = {
    sender().tell (dashboard.writeHash, self)
  }

  private def handleGetReadonlyHashCommand (command: GetReadonlyHash): Unit = {
    sender().tell (dashboard.readonlyHash, self)
  }

  private def handleGetNameCommand (command: GetName): Unit = {
    sender().tell (dashboard.name, self)
  }

  private def handlerGetDashboard(command: GetDashboard): Unit = {
    sender().tell(dashboard, self)
  }

  private def handleAddItemCommand (command: AddItem): Unit = {
    val addItem: Dashboard.AddItem = command.asInstanceOf[Dashboard.AddItem]
    dashboard.items + addItem.name
    notifyWatchers ()
  }

  private def handleRemoveItemCommand (command: RemoveItem): Unit = {
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