package controllers.forms;

public class CreateDashboard3 {

    private String ownerName;

    private String ownerEmail;

    public CreateDashboard3() {
    }

    public CreateDashboard3(String ownerName, String ownerEmail) {
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
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
}
