package actors;

import java.util.HashMap;
import java.util.Map;

import org.jongo.MongoCollection;

import play.Play;
import uk.co.panaxiom.playjongo.PlayJongo;

/**
 * Created by vasek on 11. 12. 2016.
 */
public class Dashboard {

    public static PlayJongo jongo = Play.application().injector().instanceOf(PlayJongo.class);

    public static MongoCollection dashboards() {
        return jongo.getCollection("dashboards");
    }

    private String name;

    public Dashboard(String name) {
        this.name = name;
    }

    public void insert() {
        dashboards().save(this);
    }

    public String getName() {
        return name;
    }

    public static final class Watch {

    }

    public static final class Unwatch {

    }

    public static final class Data {
        public Map<String, Item> items = new HashMap<>();

        public Data(Map<String, Item> items) {
            this.items = items;
        }
    }

    public static final class IncrementItem {
        public String name;

        public IncrementItem(String item) {
            this.name = item;
        }
    }

    public static final class DecrementItem {
        public String name;

        public DecrementItem(String item) {
            this.name = item;
        }
    }

    public static final class AddItem {
        public String name;

        public AddItem(String item) {
            this.name = item;
        }
    }

    public static final class RemoveItem {
        public String name;

        public RemoveItem(String item) {
            this.name = item;
        }
    }

    public static final class GetHash {

    }

    public static final class GetName {

    }


}
