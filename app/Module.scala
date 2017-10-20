
import com.google.inject.AbstractModule
import controllers.{DashboardView, DashboardViewBuilder, Scodash}
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[Scodash](Scodash.Name, _ => Scodash.props)
    bindActor[DashboardView](DashboardView.Name, _ => DashboardView.props)
    bindActor[DashboardViewBuilder](DashboardViewBuilder.Name, _ => DashboardViewBuilder.props)
  }

}

