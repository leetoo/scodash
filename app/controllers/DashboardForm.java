package controllers;

import play.data.validation.Constraints;

public class DashboardForm {

    @Constraints.Required
    private String name;

    public DashboardForm() {
    }

    public String validate() {
        if (!unique(name)) {
            return "Dashboard of that name already exists.";
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private boolean unique(String name) {
        return true;
    }
}
