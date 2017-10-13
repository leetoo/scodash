package controllers

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Inject
import controllers.Forms.CreateDashboardItems
import controllers.Scodash.Command.CreateNewDashboard
import org.json4s.native.Serialization.write
import org.json4s.native._
import org.json4s.{DefaultFormats, _}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.duration._


class ApplicationScala @Inject() (system: ActorSystem) extends Controller {

  val SESSION_DASHBOARD = "dashboard"

  implicit val timeout: Timeout = 5.seconds
  implicit lazy val formats = DefaultFormats

  val scodashActor = system.actorOf(Scodash.props, Scodash.Name)
  val dashboardView = system.actorOf(DashboardView.props, DashboardView.Name)
  val dashboardViewBuilder = system.actorOf(DashboardViewBuilder.props, DashboardViewBuilder.Name)

  def index() = Action {
    Ok(views.html.index());
  }

  val newDashboardForm = Form(
    mapping(
      "name" -> text,
      "description" -> text,
      "style" -> text
    )(Forms.NewDashboard.apply)(Forms.NewDashboard.unapply)
  )

  def showNewDashboard() = Action { implicit request =>
    request.session.get(SESSION_DASHBOARD) match {
      case Some(sessDash) =>
        Ok(views.html.createDashboardNew(newDashboardForm.fill(JsonMethods.parse(sessDash).extract[Forms.NewDashboard])))
      case _ =>
        Ok(views.html.createDashboardNew(newDashboardForm))
    }
  }

  val dashboardItemsForm = Form(
    mapping(
      "items" -> set(text)
    )(Forms.CreateDashboardItems.apply)(Forms.CreateDashboardItems.unapply)
  )

  def processNewDashboard() = Action { implicit request =>
    newDashboardForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.createDashboardNew(newDashboardForm))
      },
      dashboardData => {
        var dashboard = new Forms.Dashboard()
        request.session.get(SESSION_DASHBOARD) match {
          case Some(sessDash) => dashboard = JsonMethods.parse(sessDash).extract[Forms.Dashboard]
          case _ =>
        }
        val updatedDashboard = dashboard.updateNameDescStyle(dashboardData.name, dashboardData.description, dashboardData.style)
        Ok(views.html.createDashboardItems(dashboardItemsForm.fill(new CreateDashboardItems(updatedDashboard)))).withSession(SESSION_DASHBOARD -> write(updatedDashboard))
      }
    )
  }

  def showDashboardItems() = Action { implicit request =>
    request.session.get(SESSION_DASHBOARD) match {
      case Some(sessDash) => Ok(views.html.createDashboardItems(dashboardItemsForm.fill(JsonMethods.parse(sessDash).extract[CreateDashboardItems])))
      case _ => Ok(views.html.createDashboardItems(dashboardItemsForm))
    }
  }

  val itemForm = Form(
    mapping(
      "itemName" -> text
    )(Forms.Item.apply)(Forms.Item.unapply)
  )

  def addItem() = Action { implicit request =>
    itemForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.createDashboardItems(dashboardItemsForm))
      },
      formData => {
        val sessDash = JsonMethods.parse(request.session.get(SESSION_DASHBOARD).get).extract[Forms.Dashboard]
        val updatedDashboard = sessDash.updateItems(sessDash.items + formData.itemName)
        Ok(views.html.createDashboardItems(dashboardItemsForm.fill(new Forms.CreateDashboardItems(updatedDashboard)))).withSession(SESSION_DASHBOARD -> write(updatedDashboard))
      }
    )
  }

  def removeItem() = Action { implicit request =>
    itemForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.createDashboardItems(dashboardItemsForm))
      },
      formData => {
        val sessDash = JsonMethods.parse(request.session.get(SESSION_DASHBOARD).get).extract[Forms.Dashboard]
        val updatedDashboard = sessDash.updateItems(sessDash.items - formData.itemName)
        Ok(views.html.createDashboardItems(dashboardItemsForm.fill(new Forms.CreateDashboardItems(updatedDashboard)))).withSession(SESSION_DASHBOARD -> write(updatedDashboard))
      }
    )

  }

  val dashboardOwnerForm = Form(
    mapping(
      "name" -> text,
      "email" -> email
    )(Forms.DashboardOwner.apply)(Forms.DashboardOwner.unapply)
  )

  def showDashboardOwner() = Action { implicit request =>
    Ok(views.html.createDashboardOwner(dashboardOwnerForm))
  }

  def processDashboardOwner() = Action.async { implicit request =>
    dashboardOwnerForm.bindFromRequest.fold(
      formWithErrors => {
        Future(BadRequest(views.html.createDashboardOwner(formWithErrors)))
      },
      ownerData => {
        val sessDash = JsonMethods.parse(request.session.get(SESSION_DASHBOARD).get).extract[Forms.Dashboard]
        (scodashActor ? CreateNewDashboard(
          sessDash.name,
          sessDash.description,
          sessDash.style,
          sessDash.items.map{i => i -> ItemFO(i)}.toMap[String, ItemFO],
          ownerData.ownerName,
          ownerData.ownerEmail)).mapTo[FullResult[DashboardFO]].map {
            r => Ok(views.html.createdDashboard(Forms.CreatedDashboard(r.value.name, r.value.writeHash, r.value.readonlyHash)))
          }
      }
    )
  }


  def dashboard(hash: String) = Action.async {
    (dashboardView ? DashboardView.FindDashboardByWriteHash(hash)).mapTo[FullResult[List[JObject]]].map {
      result => {
        val dashboardFO = result.value.head.extract[DashboardFO]
        Ok(views.html.dashboard(dashboardFO))
      }
    }
  }

}
