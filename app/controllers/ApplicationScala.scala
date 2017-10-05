package controllers

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Inject
import controllers.Scodash.Command.CreateNewDashboard
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}

import scala.compat.java8.FutureConverters
import scala.concurrent.Future
import scala.concurrent.duration._


class ApplicationScala @Inject() (system: ActorSystem) extends Controller {

  val SESSION_DASHBOARD_NAME = "name"
  val SESSION_DASHBOARD_DESCRIPTION = "description"
  val SESSION_DASHBOARD_TYPE = "type"
  val SESSION_DASHBOARD_ITEMS = "items"
  val SESSION_DASHBOARD_ITEMS_ITEMS = "items"
  val SESSION_DASHBOARD_OWNER_NAME = "ownerName"
  val SESSION_DASHBOARD_OWNER_EMAIL = "ownerEmail"

  implicit val timeout: Timeout = 5.seconds

  val scodashActor = system.actorOf(Scodash.props, Scodash.Name)
  val dashboardView = system.actorOf(DashboardView.props, DashboardView.Name)
  val dashboardViewBuild = system.actorOf(DashboardViewBuilder.props, DashboardViewBuilder.Name)



  val dashboardOwnerForm = Form(
    mapping(
      "name" -> text,
      "email" -> email
    )(Forms.DashboardOwner.apply)(Forms.DashboardOwner.unapply)
  )

  def showDashboardOwner() = Action { implicit request =>
    //dashboardOwnerForm.fill(DashboardOwner(request.session(SESSION_DASHBOARD_OWNER_NAME), request.session(SESSION_DASHBOARD_OWNER_EMAIL)))
    //dashboardOwnerForm.fill(DashboardOwner(request.session(SESSION_DASHBOARD_OWNER_NAME), request.session(SESSION_DASHBOARD_OWNER_EMAIL)))
    Ok(views.html.createDashboardOwner(dashboardOwnerForm))
  }

  def processDashboardOwner() = Action.async { implicit request =>
    dashboardOwnerForm.bindFromRequest.fold(
      formWithErrors => {
        Future(BadRequest(views.html.createDashboardOwner(formWithErrors)))
      },
      ownerData => {
        (scodashActor ? CreateNewDashboard(
          request.session(SESSION_DASHBOARD_NAME),
          request.session(SESSION_DASHBOARD_DESCRIPTION),
          request.session(SESSION_DASHBOARD_TYPE),
          Map("Vasek" -> ItemFO("Vasek")),
          ownerData.name,
          ownerData.email)).mapTo[FullResult[DashboardFO]].map {
            r => Ok(views.html.createdDashboard(Forms.CreatedDashboard(r.value.name, r.value.writeHash, r.value.readonlyHash)))
          }
      }
    )
  }

  def dashboard(hash: String) = Action.async {
    (dashboardView ? DashboardView.FindDashboardByWriteHash(hash)).mapTo[FullResult[DashboardFO]].map {
      r => Ok(views.html.dashboard(r.value))
    }
  }

}
