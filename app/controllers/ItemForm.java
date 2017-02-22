package controllers;

import play.data.validation.Constraints;

public class ItemForm {

    @Constraints.Required
    private String name;

    @Constraints.Required
    private String hash;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
