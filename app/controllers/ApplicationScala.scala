package controllers

import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}

class ApplicationScala extends Controller {

  val dashboardOwnerForm = Form(
    mapping(
      "name" -> text,
      "email" -> email
    )(Forms.DashboardOwner.apply)(Forms.DashboardOwner.unapply)
  )


  def processDashboardOwner() = Action { implicit request =>
    dashboardOwnerForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.createDashboardOwner(formWithErrors))
      },
      ownerData => {
        Ok(views.html.createdDashboard(Forms.CreatedDashboard(ownerData.name, "xxx", "yyy")))
      }
    )


    

//    val createDashboard3Form: CreateDashboardOwner = formFactory.form(classOf[CreateDashboardOwner]).bindFromRequest(request).get
//    session(SESSION_DASHBOARD_OWNER_NAME, createDashboard3Form.getOwnerName)
//    session(SESSION_DASHBOARD_OWNER_EMAIL, createDashboard3Form.getOwnerEmail)
//
//    val itemsNodes: ArrayNode = Json.parse(session(SESSION_DASHBOARD_ITEMS)).get(SESSION_DASHBOARD_ITEMS_ITEMS).asInstanceOf[ArrayNode]
//    val items: util.Map[String, ItemFO] = IteratorUtils.toList(itemsNodes.elements).stream.map((node: JsonNode) => node.asText).collect(Collectors.toMap((item: String) => item, (item: String) => new ItemFO(item.toString)))
//
//    try {
//      val fr: FullResult[Any] = FutureConverters.toJava(ask(scodashActor, new Nothing(session(SESSION_DASHBOARD_NAME), session(SESSION_DASHBOARD_DESCRIPTION), session(SESSION_DASHBOARD_TYPE), JavaConverters.mapAsScalaMapConverter(items).asScala, session(SESSION_DASHBOARD_OWNER_NAME), session(SESSION_DASHBOARD_OWNER_EMAIL)), TIMEOUT_MILLIS)).toCompletableFuture.get.asInstanceOf[FullResult[Any]]
//      val dashboardFO: DashboardFO = fr.toOption.get.asInstanceOf[DashboardFO]
//      val createdDashboard: Forms.CreatedDashboard = new Forms.CreatedDashboard(dashboardFO.name, dashboardFO.writeHash, dashboardFO.readonlyHash)
//      val createdDashboardForm: Form[Forms.CreatedDashboard] = formFactory.form(classOf[Forms.CreatedDashboard]).fill(createdDashboard)
//      return Ok(views.html.createdDashboard.render(createdDashboardForm))
//    } catch {
//      case e: Exception =>
//        return internalServerError
//    }
//    Ok("Hello")
  }

}
