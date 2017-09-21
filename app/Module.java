import actors.JavaDashboardParentActor;
import actors.ScodashActor;
import com.google.inject.AbstractModule;

import actors.UserActor;
import actors.UserParentActor;
import play.libs.akka.AkkaGuiceSupport;

public class Module extends AbstractModule implements AkkaGuiceSupport {
    @Override
    protected void configure() {
        //bindActor(JavaDashboardParentActor.class, "dashboardParentActor");
        bindActor(ScodashActor.class, "scodashActor");
        bindActor(UserParentActor.class, "userParentActor");
        bindActorFactory(UserActor.class, UserActor.Factory.class);
        //bindActorFactory(DashboardActor.class, DashboardActor.Factory.class);
    }
}
