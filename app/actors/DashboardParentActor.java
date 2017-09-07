package actors;

import java.util.HashMap;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import play.libs.akka.InjectedActorSupport;
import pojo.Dashboard;

/**
 * Created by vasek on 19. 11. 2016.
 */
public class DashboardParentActor extends UntypedActor implements InjectedActorSupport {

    public static class Create {
        private Dashboard dashboard;

        public Create(Dashboard dashboard) {
            this.dashboard = dashboard;
        }
    }

    public static class GetDashboard {
        private String writeHash;

        public GetDashboard(String writeHash) {
            this.writeHash = writeHash;
        }
    }

    private Map<String, ActorRef> dashboardActors = new HashMap<>();

//    @Inject
//    public DashboardParentActor(DashboardActor.Factory childFactory) {
//        this.childFactory = childFactory;
//    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof DashboardParentActor.Create) {
            DashboardParentActor.Create create = (DashboardParentActor.Create) message;
            ActorRef child = context().actorOf(DashboardActor.props(create.dashboard));
            //ActorRef child = injectedChild(() -> childFactory.create(create.dashboard), "dashboardActor-" + create.dashboard.getWriteHash());
            dashboardActors.put(create.dashboard.writeHash(), child);
            sender().tell(child, self());
        }

        if (message instanceof DashboardParentActor.GetDashboard) {
            DashboardParentActor.GetDashboard getDashboard = (DashboardParentActor.GetDashboard) message;
            ActorRef dashboard = dashboardActors.get(getDashboard.writeHash);
            sender().tell(dashboard, self());
        }
    }

}

