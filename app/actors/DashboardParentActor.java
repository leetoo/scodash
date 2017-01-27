package actors;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import play.libs.akka.InjectedActorSupport;

/**
 * Created by vasek on 19. 11. 2016.
 */
public class DashboardParentActor extends UntypedActor implements InjectedActorSupport {

    public static class Create {
        private String name;
        private String hash;

        public Create(String name, String hash) {
            this.name = name;
            this.hash = hash;
        }
    }

    public static class GetDashboard {
        private String hash;

        public GetDashboard(String hash) {
            this.hash = hash;
        }
    }

    private DashboardActor.Factory childFactory;
    private Map<String, ActorRef> dashboardActors = new HashMap<>();

    @Inject
    public DashboardParentActor(DashboardActor.Factory childFactory) {
        this.childFactory = childFactory;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof DashboardParentActor.Create) {
            DashboardParentActor.Create create = (DashboardParentActor.Create) message;
            ActorRef child = injectedChild(() -> childFactory.create(create.name, create.hash), "dashboardActor-" + create.hash);
            dashboardActors.put(create.hash, child);
            sender().tell(child, self());
        }

        if (message instanceof DashboardParentActor.GetDashboard) {
            DashboardParentActor.GetDashboard getDashboard = (DashboardParentActor.GetDashboard) message;
            ActorRef dashboard = dashboardActors.get(getDashboard.hash);
            sender().tell(dashboard, self());
        }
    }

}

