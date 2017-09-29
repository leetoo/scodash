package controllers

import java.util.Date

import akka.actor.Props
import akka.persistence.query.EventEnvelope

import scala.collection.mutable

trait DashboardReadModel{
  def indexRoot = "inventory"
  def entityType = Dashboard.EntityType
}

object DashboardViewBuilder{
  val Name = "dashboard-view-builder"
  case class DashboardRM(id: String, name: String, description: String, style: String,
                         items: mutable.Map[String, ItemFO] = mutable.Map(), ownerName: String,
                         ownerEmail: String, readonlyHash: String,
                         writeHash: String,deleted: Boolean = false) extends  ReadModelObject
  def props = Props[DashboardViewBuilder]
}

class DashboardViewBuilder extends DashboardReadModel with ViewBuilder[DashboardViewBuilder.DashboardRM]{
  import ViewBuilder._
  import DashboardViewBuilder._
  import Dashboard.Event._
  import context.dispatcher

  def projectionId = "dashboard-view-builder"

  def actionFor(bookId:String, env:EventEnvelope) = env.event match {
    case DashboardCreated(dashboard) =>
      log.info("Saving a new dashboard entity into the elasticsearch index: {}", dashboard)
      val bookRM = DashboardRM(dashboard.id, dashboard.name, dashboard.description, dashboard.style,
        dashboard.items, dashboard.ownerName, dashboard.ownerEmail, dashboard.readonlyHash, dashboard.writeHash, dashboard.deleted )
      InsertAction(dashboard.id, bookRM)
  }
}


object DashboardView{
  val Name = "dashboard-view"
  case class FindDashboardByWriteHash(writeHash:String)
  case class FindDashboardByReadonlyHash(readonlyHash:String)
  def props = Props[DashboardView]
}

class DashboardView extends DashboardReadModel with AbstractBaseActor with ElasticsearchSupport{
  import DashboardView._
  import ElasticsearchApi._
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