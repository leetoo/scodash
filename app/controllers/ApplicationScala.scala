package controllers

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Inject
import controllers.Scodash.Command.CreateNewDashboard
import org.json4s.native.Json
import org.json4s.{DefaultFormats, _}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller, Session}

import scala.concurrent.Future
import scala.concurrent.duration._
import org.json4s.native._
import org.json4s.native.JsonMethods._

import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}


class ApplicationScala @Inject() (system: ActorSystem) extends Controller {

  val SESSION_DASHBOARD = "dashboard"

  val SESSION_DASHBOARD_NAME = "name"
  val SESSION_DASHBOARD_DESCRIPTION = "description"
  val SESSION_DASHBOARD_TYPE = "type"
  val SESSION_DASHBOARD_ITEMS = "items"
  val SESSION_DASHBOARD_ITEMS_ITEMS = "items"
  val SESSION_DASHBOARD_OWNER_NAME = "ownerName"
  val SESSION_DASHBOARD_OWNER_EMAIL = "ownerEmail"

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
      case Some(sessDash) => Ok(views.html.createDashboardNew(newDashboardForm.fill(JsonMethods.parse(sessDash).extract[Forms.NewDashboard])))
      case _ => Ok(views.html.createDashboardNew(newDashboardForm))
    }
  }

  val dashboardItemsForm = Form(
    mapping(
      "items" -> list(text)
    )(Forms.CreateDashboardItems.apply)(Forms.CreateDashboardItems.unapply)
  )

  def processNewDashboard() = Action { implicit request =>
    newDashboardForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.createDashboardNew(newDashboardForm))
      },
      dashboardData => {
        Ok(views.html.createDashboardItems(dashboardItemsForm)).withSession(SESSION_DASHBOARD -> write(dashboardData))
      }
    )
  }

  def showDashboardItems() = Action { implicit request =>
    request.session.get(SESSION_DASHBOARD) match {
      case Some(sessDash) => Ok(views.html.createDashboardItems(dashboardItemsForm.fill(JsonMethods.parse(sessDash).extract[Forms.CreateDashboardItems])))
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
        val dashboard1 = sessDash.updateItems(sessDash.items ::: List(formData.itemName))
        Ok(views.html.createDashboardItems(dashboardItemsForm)).withSession(SESSION_DASHBOARD -> write(dashboard1))
      }
    )
  }

  def removeItem() = Action { implicit request =>
    BadRequest(views.html.createDashboardItems(dashboardItemsForm))
  }



//  def removeItem: Result = {
//    val removeItem = formFactory.form(classOf[Item]).bindFromRequest(request).get
//    val itemsArray = Json.parse(session(SESSION_DASHBOARD_ITEMS)).get(SESSION_DASHBOARD_ITEMS).asInstanceOf[ArrayNode]
//    val updateItems = IteratorUtils.toList(itemsArray.elements).stream.filter((item: JsonNode) => !(removeItem.getItemName == item.asText)).collect(Collectors.toList)
//    val jsonItems = Json.newObject.set(SESSION_DASHBOARD_ITEMS, Json.newArray.addAll(updateItems))
//    session(SESSION_DASHBOARD_ITEMS, jsonItems.toString)
//    showDashboardItems
//  }


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
