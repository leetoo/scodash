
import com.google.inject.AbstractModule
import com.google.inject.name.Named
import controllers.actors.Scodash
import controllers.{DashboardView, DashboardViewBuilder}
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    //bindActor[Scodash](Scodash.Name, _ => Scodash.props)
    //bindActor[DashboardView](DashboardView.Name, _ => DashboardView.props)
    bindActor[DashboardViewBuilder](DashboardViewBuilder.Name, _ => DashboardViewBuilder.props)
  }

  import akka.actor.Actor
  import akka.actor.ActorRef
  import akka.actor.ActorSystem
  import akka.actor.Props
  import com.google.inject.Provides

  @Provides
  @Named(DashboardView.Name) def dashboardViewActorRef(system: ActorSystem): ActorRef = system.actorOf(DashboardView.props)

  @Provides
  @Named(Scodash.Name) def scodashActorRef(system: ActorSystem): ActorRef = system.actorOf(Scodash.props)

}

