import com.google.inject.AbstractModule;

import actors.DashboardActor;
import actors.DashboardParentActor;
import actors.UserActor;
import actors.UserParentActor;
import play.libs.akka.AkkaGuiceSupport;

public class Module extends AbstractModule implements AkkaGuiceSupport {
    @Override
    protected void configure() {
        bindActor(DashboardParentActor.class, "dashboardParentActor");
        bindActor(UserParentActor.class, "userParentActor");
        bindActorFactory(UserActor.class, UserActor.Factory.class);
        //bindActorFactory(DashboardActor.class, DashboardActor.Factory.class);
    }
}
