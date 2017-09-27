package forms;

public class CreateDashboardOwner {

    private String ownerName;

    private String ownerEmail;

    public CreateDashboardOwner() {
    }

    public CreateDashboardOwner(String ownerName, String ownerEmail) {
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
