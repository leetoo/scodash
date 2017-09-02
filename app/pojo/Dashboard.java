package pojo;

import actors.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vasek on 11. 12. 2016.
 */
public class Dashboard {

    private String name;
    private String description;
    private String type;
    List<String> items = new ArrayList<>();
    private String ownerName;
    private String ownerEmail;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
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
