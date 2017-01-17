package actors;

/**
 * Created by vasek on 19. 11. 2016.
 */

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.assistedinject.Assisted;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import play.Configuration;
import play.libs.Json;

/**
 * The broker between the WebSocket and the StockActor(s).  The UserActor holds the connection and sends serialized
 * JSON data to the client.
 */
public class UserActor extends UntypedActor {

    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private ActorRef out;
    private Configuration configuration;
    //private ActorRef stocksActor;
    private ActorRef dashboardActor;

    @Inject
    public UserActor(@Assisted ActorRef out
                     /*@Named("stocksActor") ActorRef stocksActor,*/
                     /*@Named("dashboardActor") ActorRef dashboardActor*/,
                     Configuration configuration) {
        this.out = out;
        //this.dashboardActor = dashboardActor;
//        this.stocksActor = stocksActor;
        this.configuration = configuration;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();

//        configureDefaultStocks();

        //dashboardActor.tell(new Dashboard.Watch(), self());
    }

//    public void configureDefaultStocks() {
//        List<String> defaultStocks = configuration.getStringList("default.stocks");
//        logger.info("Creating user actor with default stocks {}", defaultStocks);
//
//        for (String stockSymbol : defaultStocks) {
//            stocksActor.tell(new Stock.Watch(stockSymbol), self());
//        }
//    }

    public void onReceive(Object msg) throws Exception {

//        if (msg instanceof Stock.Update) {
//            Stock.Update stockUpdate = (Stock.Update) msg;
//            // push the stock to the client
//            JsonNode message =
//                    Json.newObject()
//                            .put("type", "stockupdate")
//                            .put("symbol", stockUpdate.symbol)
//                            .put("price", stockUpdate.price);
//
//            logger.debug("onReceive: " + message);
//
//            out.tell(message, self());
//        }
//
//        if (msg instanceof Stock.History) {
//            Stock.History stockHistory = (Stock.History) msg;
//            // push the history to the client
//            ObjectNode message =
//                    Json.newObject()
//                            .put("type", "stockhistory")
//                            .put("symbol", stockHistory.symbol);
//
//            ArrayNode historyJson = message.putArray("history");
//            for (Double price : stockHistory.history) {
//                historyJson.add(price);
//            }
//
//            logger.debug("onReceive: " + message);
//
//            out.tell(message, self());
//        }

        if (msg instanceof Dashboard.AddItem) {
            Dashboard.AddItem addItem = (Dashboard.AddItem) msg;
            ObjectNode message =
                    Json.newObject()
                        .put("type", "additem")
                        .put("item", addItem.item);
            logger.debug("onReceive: " + message);
            out.tell(message, self());
        }

        if (msg instanceof Dashboard.DecrementItem) {
            Dashboard.DecrementItem decrementItem = (Dashboard.DecrementItem) msg;
            ObjectNode message =
                    Json.newObject()
                            .put("type", "decrementitem")
                            .put("item", decrementItem.item);
            logger.debug("onReceive: " + message);
            out.tell(message, self());
        }

        if (msg instanceof Dashboard.IncrementItem) {
            Dashboard.IncrementItem incrementItem = (Dashboard.IncrementItem) msg;
            ObjectNode message =
                    Json.newObject()
                            .put("type", "incrementitem")
                            .put("item", incrementItem.item);
            logger.debug("onReceive: " + message);
            out.tell(message, self());
        }

        if (msg instanceof Dashboard.Data) {
            Dashboard.Data data = (Dashboard.Data)msg;
            ArrayNode items = Json.newArray();
            for (Item item : data.items.values()) {
                items.addObject().put("name", item.getName()).put("score", item.getScore());
            }

            ObjectNode message = Json.newObject();
            message.set("type", Json.toJson("data"));
            message.set("items", items);
            out.tell(message, self());
        }

        if (msg instanceof JsonNode) {
            // From browser
            // When the user types in a stock in the upper right corner, this is triggered
            JsonNode json = (JsonNode) msg;
            logger.debug("onReceive: " + msg);
            final String operation = json.get("operation").textValue();
            final String item = json.get("item").textValue();
            if ("increment".equals(operation)) {
                dashboardActor.tell(new Dashboard.IncrementItem(item), self());
            } else if ("decrement".equals(operation)) {
                dashboardActor.tell(new Dashboard.DecrementItem(item), self());
            } else {
                logger.error("No operation in JSON");
            }
        }
    }

    public interface Factory {
        Actor create(ActorRef out);
    }
}

