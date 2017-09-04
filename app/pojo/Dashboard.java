package pojo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vasek on 11. 12. 2016.
 */
public class Dashboard {

    private String name;
    private String description;
    private String type;
    private Map<String, Item> items = new HashMap<>();
    private String ownerName;
    private String ownerEmail;
    private String readOnlyHash;
    private String writeHash;

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

    public String getReadOnlyHash() {
        return readOnlyHash;
    }

    public void setReadOnlyHash(String readOnlyHash) {
        this.readOnlyHash = readOnlyHash;
    }

    public String getWriteHash() {
        return writeHash;
    }

    public void setWriteHash(String writeHash) {
        this.writeHash = writeHash;
    }

    public Map<String, Item> getItems() {
        return items;
    }

    public void setItems(final Map<String, Item> items) {
        this.items = items;
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

    public static final class GetWriteHash {

    }
    public static final class GetReadonlyHash {

    }

    public static final class GetName {

    }


}
