package actors;

import akka.actor.AbstractActor;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.PersistentActor;
import com.google.inject.assistedinject.Assisted;
import pojo.Dashboard;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by vasek on 11. 12. 2016.
 */
public class DashboardActor extends AbstractPersistentActor {

    private Dashboard dashboard;

    private final HashSet<ActorRef> watchers = new HashSet<ActorRef>();

    @Inject
    public DashboardActor(@Assisted("dashboard") Dashboard dashboard) {
        this.dashboard = dashboard;
    }


    @Override
    public void preStart() throws Exception {
        super.preStart();
    }

    @Override
    public void onReceive(Object message) throws Throwable {


        if (message instanceof Dashboard.IncrementItem) {
            Dashboard.IncrementItem incrementItem = (Dashboard.IncrementItem)message;
            Item item = dashboard.getItems().get(incrementItem.name);
            if (item != null) {
                item.increment();
            }
            notifyWatchers();
        }

        if (message instanceof Dashboard.DecrementItem) {
            Dashboard.DecrementItem decrementItem = (Dashboard.DecrementItem)message;
            Item item = dashboard.getItems().get(decrementItem.name);
            if (item != null) {
                item.decrement();
            }
            notifyWatchers();
        }

        if (message instanceof Dashboard.Watch) {
            Dashboard.Data data = new Dashboard.Data(dashboard.getItems());
            sender().tell(data, self());
            watchers.add(sender());
        }

        if (message instanceof Dashboard.Unwatch) {
            watchers.remove(sender());
        }

        if (message instanceof Dashboard.GetWriteHash) {
            sender().tell(dashboard.getWriteHash(), self());
        }

        if (message instanceof Dashboard.GetReadonlyHash) {
            sender().tell(dashboard.getReadOnlyHash(), self());
        }


        if (message instanceof Dashboard.GetName) {
            sender().tell(dashboard.getName(), self());
        }

        if (message instanceof Dashboard.AddItem) {
            Dashboard.AddItem addItem = (Dashboard.AddItem)message;
            dashboard.getItems().put(addItem.name, new Item(addItem.name));

            notifyWatchers();

        }

        if (message instanceof Dashboard.RemoveItem) {
            Dashboard.RemoveItem removeItem = (Dashboard.RemoveItem)message;
            dashboard.getItems().remove(removeItem.name);

            notifyWatchers();

        }



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

    public interface Factory {
        Actor create(@Assisted("name") String name, @Assisted("hash") String hash);
    }
}
