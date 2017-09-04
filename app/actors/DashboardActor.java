package actors;

import java.util.HashSet;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;

import akka.actor.AbstractActor;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import akka.persistence.PersistentActor;
import pojo.Dashboard;
import pojo.Item;

/**
 * Created by vasek on 11. 12. 2016.
 */
public class DashboardActor extends PersistentActor {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardActor.class);

    private Dashboard dashboard;

    private final HashSet<ActorRef> watchers = new HashSet<>();

    @Inject
    public DashboardActor(@Assisted("dashboard") Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    
    private void handleCommand(Dashboard.IncrementItem command) {
        Item item = dashboard.getItems().get(command.name);
        if (item != null) {
            item.increment();
        }
        notifyWatchers();
    }

    private void handleCommand(Dashboard.DecrementItem command) {
        Item item = dashboard.getItems().get(command.name);
        if (item != null) {
            item.decrement();
        }
        notifyWatchers();
    }

    private void handleCommand(Dashboard.Watch command) {
        Dashboard.Data data = new Dashboard.Data(dashboard.getItems());
        getSender().tell(data, self());
        watchers.add(getSender());
    }

    private void handleCommand(Dashboard.Unwatch command) {
        watchers.remove(getSender());
    }

    private void handleCommand(Dashboard.GetWriteHash command) {
        getSender().tell(dashboard.getWriteHash(), self());
    }

    private void handleCommand(Dashboard.GetReadonlyHash command) {
        getSender().tell(dashboard.getReadOnlyHash(), self());
    }

    private void handleCommand(Dashboard.GetName command) {
        getSender().tell(dashboard.getName(), self());
    }

    private void handleCommand(Dashboard.AddItem command) {
        Dashboard.AddItem addItem = (Dashboard.AddItem)command;
        dashboard.getItems().put(addItem.name, new Item(addItem.name));

        notifyWatchers();
    }

    private void handleCommand(Dashboard.RemoveItem command) {
        Dashboard.RemoveItem removeItem = (Dashboard.RemoveItem)command;
        dashboard.getItems().remove(removeItem.name);

        notifyWatchers();
    }


    //@Override
    public void onReceive(Object message) throws Throwable {


//        if (message instanceof Dashboard.IncrementItem) {
//            Dashboard.IncrementItem incrementItem = (Dashboard.IncrementItem)message;
//            Item item = dashboard.getItems().get(incrementItem.name);
//            if (item != null) {
//                item.increment();
//            }
//            notifyWatchers();
//        }
//
//        if (message instanceof Dashboard.DecrementItem) {
//            Dashboard.DecrementItem decrementItem = (Dashboard.DecrementItem)message;
//            Item item = dashboard.getItems().get(decrementItem.name);
//            if (item != null) {
//                item.decrement();
//            }
//            notifyWatchers();
//        }
//
//        if (message instanceof Dashboard.Watch) {
//            Dashboard.Data data = new Dashboard.Data(dashboard.getItems());
//            getSender().tell(data, self());
//            watchers.add(getSender());
//        }

//        if (message instanceof Dashboard.Unwatch) {
//            watchers.remove(getSender());
//        }

//        if (message instanceof Dashboard.GetWriteHash) {
//            getSender().tell(dashboard.getWriteHash(), self());
//        }

//        if (message instanceof Dashboard.GetReadonlyHash) {
//            getSender().tell(dashboard.getReadOnlyHash(), self());
//        }
//
//
//        if (message instanceof Dashboard.GetName) {
//            getSender().tell(dashboard.getName(), self());
//        }
//
//        if (message instanceof Dashboard.AddItem) {
//            Dashboard.AddItem addItem = (Dashboard.AddItem)message;
//            dashboard.getItems().put(addItem.name, new Item(addItem.name));
//
//            notifyWatchers();
//
//        }
//
//        if (message instanceof Dashboard.RemoveItem) {
//            Dashboard.RemoveItem removeItem = (Dashboard.RemoveItem)message;
//            dashboard.getItems().remove(removeItem.name);
//
//            notifyWatchers();
//
//        }



//        ReceiveBuilder
//                .match(Dashboard.Item.class, latest -> {
//
//                    // add a new stock price to the history and drop the oldest
//                    //Double newPrice = stockQuote.newPrice(stockHistory.peekLast());
//                    //stockHistory.add(newPrice);
//                    //stockHistory.remove();
//                    // notify watchers
//                    //watchers.forEach(watcher -> watcher.tell(new Stock.Update(symbol, newPrice), self()));
//
//                    // add new name on showDashboard
//
//                })
//                .match(Stock.Watch.class, watch -> {
//                    // reply with the stock history, and add the sender as a watcher
//                    //final Double[] clone = stockHistory.toArray(new Double[]{});
//                    //sender().tell(new Stock.History(symbol, clone), self());
//                    //watchers.add(sender());
//                })
//                .match(Stock.Unwatch.class, unwatch -> {
//                    //watchers.remove(sender());
//                    //if (watchers.isEmpty()) {
//                    //    stockTick.ifPresent(Cancellable::cancel);
//                    //    context().stop(self());
//                    //}
//                });
    }

    private void notifyWatchers() {
        watchers.forEach(watcher -> watcher.tell(new Dashboard.Data(dashboard.getItems()), self()));
    }

    protected abstract AbstractActor.Receive buildReceiver(ReceiveBuilder defaultMatches);

    private ReceiveBuilder getDefaultMatches() {
        return receiveBuilder()
                .match(Dashboard.IncrementItem.class, this::handleCommand)
                .match(Dashboard.DecrementItem.class, this::handleCommand)
                .match(Dashboard.AddItem.class, this::handleCommand)
                .match(Dashboard.RemoveItem.class, this::handleCommand)
                .match(Dashboard.Watch.class, this::handleCommand)
                .match(Dashboard.GetName.class, this::handleCommand)
                .match(Dashboard.GetReadonlyHash.class, this::handleCommand)
                .match(Dashboard.GetWriteHash.class, this::handleCommand);
    }

    @Override
    public Receive createReceiveRecover() {
        return null;
    }

    @Override
    public Receive createReceive() {
        return null;
    }

    @Override
    public String persistenceId() {
        return null;
    }

    @Override
    public void deleteSnapshot(final long sequenceNr) {
        super.deleteSnapshot(sequenceNr);
    }


//    @Override
//    public Receive createReceiveRecover() {
//        LOG.debug("receiveCommand");
//        ReceiveBuilder defaultMatches = getDefaultMatches();
//        return buildReceiver(defaultMatches);
//    }
//
//
//    @Override
//    public Receive createReceive() {
//        LOG.debug("receiveCommand");
//        ReceiveBuilder defaultMatches = getDefaultMatches();
//        return buildReceiver(defaultMatches);
//
//    }
//
//    @Override
//    public String persistenceId() {
//        return dashboard.getWriteHash();
//    }
//
//    @Override
//    public void deleteSnapshot(final long sequenceNr) {
//        super.deleteSnapshot(sequenceNr);
//    }


    public interface Factory {
        Actor create(@Assisted("name") String name, @Assisted("hash") String hash);
    }
}
