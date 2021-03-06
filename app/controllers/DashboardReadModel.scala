package controllers

import akka.actor.Props
import akka.persistence.query.EventEnvelope
import controllers.actors.Scodash.Command.{FindDashboardByReadonlyHash, FindDashboardByWriteHash}
import org.joda.time.DateTime

trait DashboardReadModel{
  def indexRoot = "scodash"
  def entityType = Dashboard.EntityType
}

object DashboardViewBuilder{
  final val Name = "dashboard-view-builder"
  case class DashboardRM(id: String, name: String, description: String,
                         items: List[ItemFO] = List(), ownerName: String,
                         ownerEmail: String, readonlyHash: String,
                         writeHash: String, created: DateTime,
                         updated: DateTime, deleted: Boolean = false) extends  ReadModelObject
  def props = Props[DashboardViewBuilder]
}

class DashboardViewBuilder extends DashboardReadModel with ViewBuilder[DashboardViewBuilder.DashboardRM] {
  import Dashboard.Event._
  import DashboardViewBuilder._
  import ViewBuilder._

  def projectionId = "dashboard-view-builder"

  def actionFor(dashboardId:String, env:EventEnvelope) = env.event match {
    case DashboardCreated(dashboard) =>
      log.info("Saving a new dashboard entity into the elasticsearch index: {}", dashboard)
      val dashboardRM = DashboardRM(dashboard.id, dashboard.name, dashboard.description,
        dashboard.items, dashboard.ownerName, dashboard.ownerEmail, dashboard.readonlyHash, dashboard.writeHash,
        dashboard.created, dashboard.updated, dashboard.deleted )
      InsertAction(dashboard.id, dashboardRM)
    case DashboardUpdated(dashboard) =>
      val dashboardRM = DashboardRM(dashboard.id, dashboard.name, dashboard.description,
        dashboard.items, dashboard.ownerName, dashboard.ownerEmail, dashboard.readonlyHash, dashboard.writeHash,
        dashboard.created, dashboard.updated, dashboard.deleted )
      InsertAction(dashboard.id, dashboardRM)
  }
}


object DashboardView{
  final val Name = "dashboard-view"
  def props = Props[DashboardView]

}

class DashboardView extends DashboardReadModel with AbstractBaseActor with ElasticsearchSupport{
  import context.dispatcher

  def receive = {
    case FindDashboardByWriteHash(writeHash) =>
      val results = queryElasticsearch(s"writeHash:$writeHash")
      pipeResponse(results)
    case FindDashboardByReadonlyHash(readonlyHash) =>
      val results = queryElasticsearch(s"readonlyHash:$readonlyHash")
      pipeResponse(results)

  }
}