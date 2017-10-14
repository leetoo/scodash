package controllers;

/**
 * Created by vasek on 19. 11. 2016.
 */

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.assistedinject.Assisted;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import controllers.Application;
import play.Configuration;
import play.libs.Json;
import scala.collection.JavaConverters;
import scala.compat.java8.FutureConverters;

/**
 * The broker between the WebSocket and the StockActor(s).  The UserActor holds the connection and sends serialized
 * JSON data to the client.
 */
public class UserActor extends UntypedActor {

    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private ActorRef out;
    private Configuration configuration;
    private ActorRef dashboardActor;
    private String hash;
    private ActorRef scodashActor;

    @Inject
    public UserActor(@Assisted("hash") String hash,
                     @Assisted ActorRef out,
                     @Named("scodashActor") ActorRef scodashActor,
                     Configuration configuration) {
        this.out = out;
        this.configuration = configuration;
        this.hash = hash;
        this.scodashActor = scodashActor;
    }


    private void initDashboardActor() {
//        try {
//            this.dashboardActor = (ActorRef) FutureConverters.toJava(
//                    ask(scodashActor, new Scodash.FindDashboard(pojo.DashboardId.apply(hash)), Application.TIMEOUT_MILLIS)
//            ).toCompletableFuture().get();
//            this.dashboardActor.tell(new Dashboard.Command.Watch(), self() );
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();

        initDashboardActor();

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

        if (msg instanceof Dashboard$Command$AddItem) {
            Dashboard$Command$AddItem addItem = (Dashboard$Command$AddItem) msg;
            ObjectNode message =
                    Json.newObject()
                        .put("type", "additem")
                        .put("name", addItem.name());
            logger.debug("onReceive: " + message);
            out.tell(message, self());
        }

        if (msg instanceof Dashboard$Command$DecrementItem) {
            Dashboard$Command$DecrementItem decrementItem = (Dashboard$Command$DecrementItem) msg;
            ObjectNode message =
                    Json.newObject()
                            .put("type", "decrementitem")
                            .put("name", decrementItem.name());
            logger.debug("onReceive: " + message);
            out.tell(message, self());
        }

        if (msg instanceof Dashboard$Command$IncrementItem) {
            Dashboard$Command$IncrementItem incrementItem = (Dashboard$Command$IncrementItem) msg;
            ObjectNode message =
                    Json.newObject()
                            .put("type", "incrementitem")
                            .put("name", incrementItem.name());
            logger.debug("onReceive: " + message);
            out.tell(message, self());
        }

        if (msg instanceof Dashboard$Command$RemoveItem) {
            Dashboard$Command$RemoveItem removeItem = (Dashboard$Command$RemoveItem) msg;
            ObjectNode message =
                    Json.newObject()
                            .put("type", "removeitem")
                            .put("name", removeItem.name());
            logger.debug("onReceive: " + message);
            out.tell(message, self());
        }

        if (msg instanceof Dashboard$Command$Data) {
            Dashboard$Command$Data data = (Dashboard$Command$Data)msg;
            ArrayNode items = Json.newArray();
            for (ItemFO item : JavaConverters.mapAsJavaMapConverter(data.items()).asJava().values()) {
                items.addObject().put("name", item.name()).put("score", item.score());
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
            final String item = json.get("name").textValue();
            if ("increment".equals(operation)) {
                dashboardActor.tell(new Dashboard$Command$IncrementItem(item), self());
            } else if ("decrement".equals(operation)) {
                dashboardActor.tell(new Dashboard$Command$DecrementItem(item), self());
            } else if ("remove".equals(operation)) {
                dashboardActor.tell(new Dashboard$Command$RemoveItem(item), self());
            } else {
                logger.error("No operation in JSON");
            }
        }
    }

    public interface Factory {
        Actor create(@Assisted("hash") String hash, ActorRef out);
    }

    public String getHash() {
        return hash;
    }
}

