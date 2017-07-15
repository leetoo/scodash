package controllers.forms;

/**
 * Created by vasek on 16. 7. 2017.
 */
public class CreateDashboard1 {

    private String dashboardName;
    private String dashboardDescription;


    public CreateDashboard1() {
    }

    public CreateDashboard1(String dashboardName, String dashboardDescription) {
        this.dashboardName = dashboardName;
        this.dashboardDescription = dashboardDescription;
    }

    public String getDashboardName() {
        return dashboardName;
    }

    public void setDashboardName(String dashboardName) {
        this.dashboardName = dashboardName;
    }

    public String getDashboardDescription() {
        return dashboardDescription;
    }

    public void setDashboardDescription(String dashboardDescription) {
        this.dashboardDescription = dashboardDescription;
    }
}
