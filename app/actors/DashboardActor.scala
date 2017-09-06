package actors

import akka.actor.{Actor, ActorRef}
import akka.persistence.PersistentActor
import com.google.inject.assistedinject.Assisted
import org.slf4j.LoggerFactory
import pojo.{Dashboard, Item}

object DashboardActor {

  private val LOG = LoggerFactory.getLogger(classOf[DashboardActor])

  trait Factory {
    def create(@Assisted("name") name: String, @Assisted("hash") hash: String): Actor
  }

}

class DashboardActor extends PersistentActor {

  final private val watchers: Set[ActorRef] = Set();

  var dashboard: Dashboard = new Dashboard();

  private def handleCommand(command: Dashboard.IncrementItem): Unit = {
    val item: Item = dashboard.getItems.get(command.name)
    if (item != null) {
      item.increment()
    }
    notifyWatchers()
  }

  private def handleCommand (command: Dashboard.DecrementItem): Unit = {
    val item: Item = dashboard.getItems.get (command.name)
    if (item != null) {
      item.decrement ()
    }
    notifyWatchers ()
  }

  private def handleCommand (command: Dashboard.Watch): Unit = {
    val data: Dashboard.Data = new Dashboard.Data (dashboard.getItems)
    sender.tell (data, self)
    watchers.+(sender)
  }

  private def handleCommand (command: Dashboard.Unwatch): Unit = {
    watchers.-(sender())
  }

  private def handleCommand (command: Dashboard.GetWriteHash): Unit = {
    sender().tell (dashboard.getWriteHash, self)
  }

  private def handleCommand (command: Dashboard.GetReadonlyHash): Unit = {
    sender().tell (dashboard.getReadOnlyHash, self)
  }

  private def handleCommand (command: Dashboard.GetName): Unit = {
    sender().tell (dashboard.getName, self)
  }

  private def handleCommand (command: Dashboard.AddItem): Unit = {
    val addItem: Dashboard.AddItem = command.asInstanceOf[Dashboard.AddItem]
    dashboard.getItems.put (addItem.name, new Item (addItem.name) )
    notifyWatchers ()
  }

  def handleCommand (command: Dashboard.RemoveItem): Unit = {
    val removeItem: Dashboard.RemoveItem = command.asInstanceOf[Dashboard.RemoveItem]
    dashboard.getItems.remove (removeItem.name)
    notifyWatchers ()
  }

  private def notifyWatchers (): Unit = {
    for (watcher:ActorRef <- watchers) {
      watcher.tell(new Dashboard.Data (dashboard.getItems), self);
    }
  }

  override def receiveRecover: Receive = {
    case dsh: Dashboard => dashboard = dsh;
  }

  override def receiveCommand: Receive = {
    case cmd:Dashboard.RemoveItem =>
    case cmd:Dashboard.AddItem =>
    case cmd:Dashboard.DecrementItem =>
    case cmd:Dashboard.IncrementItem =>
      persist(dashboard)
      handleCommand(cmd)
    case cmd:Dashboard.Data =>
    case cmd:Dashboard.GetName =>
    case cmd:Dashboard.GetReadonlyHash =>
    case cmd:Dashboard.GetWriteHash =>
      handleCommand(cmd)
  }

  override def persistenceId: String = dashboard.getWriteHash
}