package actors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

/**
 * Created by vasek on 11. 12. 2016.
 */
public class DashboardActor extends UntypedActor {

    private final String name;
    private final String hash;

    private final Map<String, Item> items = new HashMap<>();
    private final HashSet<ActorRef> watchers = new HashSet<ActorRef>();

    @Inject
    public DashboardActor(@Assisted("name") String name, @Assisted("hash") String hash) {
        this.name = name;
        this.hash = hash;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        Item item1 = new Item("AAA", 5);
        items.put(item1.getName(), item1);
        Item item2 = new Item("BBB", 10);
        items.put(item2.getName(), item2);
    }

    @Override
    public void onReceive(Object message) throws Throwable {

        if (message instanceof Dashboard.AddItem) {
            Dashboard.AddItem addItem = (Dashboard.AddItem)message;
            items.put(addItem.item, new Item(addItem.item, 0));
        }

        if (message instanceof Dashboard.IncrementItem) {
            Dashboard.IncrementItem incrementItem = (Dashboard.IncrementItem)message;
            Item item = items.get(incrementItem.item);
            if (item != null) {
                item.increment();
            }
            // notify watchers
            watchers.forEach(watcher -> watcher.tell(new Dashboard.Data(items), self()));
        }

        if (message instanceof Dashboard.DecrementItem) {
            Dashboard.DecrementItem decrementItem = (Dashboard.DecrementItem)message;
            Item item = items.get(decrementItem.item);
            if (item != null) {
                item.decrement();
            }
            // notify watchers
            watchers.forEach(watcher -> watcher.tell(new Dashboard.Data(items), self()));
        }

        if (message instanceof Dashboard.Watch) {
            Dashboard.Data data = new Dashboard.Data(items);
            sender().tell(data, self());
            watchers.add(sender());
        }

        if (message instanceof Dashboard.Unwatch) {
            watchers.remove(sender());
        }

        if (message instanceof Dashboard.GetHash) {
            sender().tell(this.hash, self());
        }


//        ReceiveBuilder
//                .match(Dashboard.AddItem.class, latest -> {
//
//                    // add a new stock price to the history and drop the oldest
//                    //Double newPrice = stockQuote.newPrice(stockHistory.peekLast());
//                    //stockHistory.add(newPrice);
//                    //stockHistory.remove();
//                    // notify watchers
//                    //watchers.forEach(watcher -> watcher.tell(new Stock.Update(symbol, newPrice), self()));
//
//                    // add new item on dashboard
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

    public interface Factory {
        Actor create(@Assisted("name") String name, @Assisted("hash") String hash);
    }
}