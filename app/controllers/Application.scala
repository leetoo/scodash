package controllers

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.pattern.ask
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import akka.util.Timeout
import com.google.inject.Inject
import com.google.inject.name.Named
import controllers.Dashboard.Event.DashboardCreated
import controllers.Forms.CreateDashboardItems
import controllers.actors.Scodash.Command.CreateNewDashboard
import controllers.actors.{DashboardAccessMode, Scodash}
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTimeZone
import org.json4s.ext.JodaTimeSerializers
import org.json4s.native.Serialization.write
import org.json4s.native._
import org.json4s.{DefaultFormats, _}
import org.reactivestreams.Publisher
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class Application @Inject() (
                              cc: MessagesControllerComponents,
                              @Named(Scodash.Name) scodashActor: ActorRef,
                              @Named(DashboardView.Name) dashboardViewActor: ActorRef,
                              @Named(DashboardViewBuilder.Name) dashboardViewBuilder: ActorRef,
                              system: ActorSystem,
                              implicit val mat: Materializer)
    extends MessagesAbstractController(cc) {

  // Use a direct reference to SLF4J
  private val logger = org.slf4j.LoggerFactory.getLogger("controllers.Application")

  val SESSION_DASHBOARD = "dashboard"

  implicit val timeout: Timeout = 5.seconds
  implicit lazy val formats = DefaultFormats ++ JodaTimeSerializers.all
//  implicit private val ItemWrites = Json.writes[ItemFO]
//  implicit private val DashoboardWrites = Json.writes[DashboardFO]


  def index() = Action {
    Ok(views.html.index());
  }

  val newDashboardForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText
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
      "items" -> set(nonEmptyText)
    )(Forms.CreateDashboardItems.apply)(Forms.CreateDashboardItems.unapply)
  )

  def processNewDashboard() = Action { implicit request =>
    newDashboardForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.createDashboardNew(formWithErrors))
      },
      dashboardData => {
        var dashboard = new Forms.Dashboard()
        request.session.get(SESSION_DASHBOARD) match {
          case Some(sessDash) => dashboard = JsonMethods.parse(sessDash).extract[Forms.Dashboard]
          case _ =>
        }
        val updatedDashboard = dashboard.updateNameDescStyle(dashboardData.name, dashboardData.description)
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
        if (StringUtils.isNotBlank(formData.itemName)) {
          val updatedDashboard = sessDash.updateItems(sessDash.items + formData.itemName)
          Ok(views.html.createDashboardItems(dashboardItemsForm.fill(new Forms.CreateDashboardItems(updatedDashboard)))).withSession(SESSION_DASHBOARD -> write(updatedDashboard))
        } else {
          Ok(views.html.createDashboardItems(dashboardItemsForm.fill(new Forms.CreateDashboardItems(sessDash))))
        }
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
      "name" -> nonEmptyText,
      "email" -> email,
      "tzOffset" -> nonEmptyText

    )(Forms.DashboardOwner.apply)(Forms.DashboardOwner.unapply)
  )

  def showDashboardOwner() = Action { implicit request =>
    val sessDash = JsonMethods.parse(request.session.get(SESSION_DASHBOARD).get).extract[Forms.Dashboard]
    if (sessDash.items.size > 0) {
      Ok(views.html.createDashboardOwner(dashboardOwnerForm))
    } else {
      Ok(views.html.createDashboardItems(dashboardItemsForm.withGlobalError("At least one item should be defined in dashboard.")))
    }
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
          sessDash.items.zipWithIndex.map { case (name, id) => ItemFO(id, name) } ,
          ownerData.ownerName,
          ownerData.ownerEmail,
          DateTimeZone.forOffsetHours(Integer.valueOf(ownerData.tzOffset)/60)
          )).mapTo[FullResult[DashboardFO]].map {
          r => {
            dashboardViewBuilder ? DashboardCreated(r.value)
            Ok(views.html.createdDashboard(Forms.CreatedDashboard(r.value.name, r.value.writeHash, r.value.readonlyHash))).withNewSession
          }
        }
      }
    )
  }

  def dashboard(hash: String) = Action.async { implicit request =>
    getDashboard(hash).flatMap{
      case Some((dashboard, accessMode)) =>
        Future(Ok(views.html.dashboard(dashboard)))
      case _ =>
        Future(Ok(views.html.noDashboard()))
    }
  }

  def dashboardData(hash: String) = Action.async { implicit request =>
    getDashboard(hash).flatMap{ case Some((dashboard, accessMode)) =>
      val json = write(dashboard)
      Future(Ok(json))
    }
  }

  def dashboardsData(hashesStr: String) = Action.async { implicit request =>
    val hashes = hashesStr.split(",")
    Future.sequence(hashes.map(hash => getDashboard(hash)).toList).flatMap(ds => {
      val json = write(ds.filter(_.isDefined).map(_.get._1))
      Future(Ok(json))
    })
  }

  /**
    * Creates a websocket.  `acceptOrResult` is preferable here because it returns a
    * Future[Flow], which is required internally.
    *
    * @return a fully realized websocket.
    */
  def ws(hash: String): WebSocket = WebSocket.acceptOrResult[JsValue, JsValue] {
    case rh if sameOriginCheck(rh) =>
      wsFutureFlow(rh, hash).map { flow =>
        Right(flow)
      }.recover {
        case e: Exception =>
          logger.error("Cannot create websocket", e)
          val jsError =  play.api.libs.json.Json.obj("error" -> "Cannot create websocket")
          val result = InternalServerError(jsError)
          Left(result)
      }

    case rejected =>
      logger.error(s"Request ${rejected} failed same origin check")
      Future.successful {
        Left(Forbidden("forbidden"))
      }
  }

  /**
    * Checks that the WebSocket comes from the same origin.  This is necessary to protect
    * against Cross-Site WebSocket Hijacking as WebSocket does not implement Same Origin Policy.
    *
    * See https://tools.ietf.org/html/rfc6455#section-1.3 and
    * http://blog.dewhurstsecurity.com/2013/08/30/security-testing-html5-websockets.html
    */
  def sameOriginCheck(rh: RequestHeader): Boolean = {
    rh.headers.get("Origin") match {
      case Some(originValue) if originMatches(originValue) =>
        logger.debug(s"originCheck: originValue = $originValue")
        true

      case Some(badOrigin) =>
        logger.error(s"originCheck: rejecting request because Origin header value $badOrigin is not in the same origin")
        false

      case None =>
        logger.error("originCheck: rejecting request because no Origin header found")
        false
    }
  }

  /**
    * Returns true if the value of the Origin header contains an acceptable value.
    */
  def originMatches(origin: String): Boolean = {
    val host = sys.env("HOST")
    origin.contains(host)
  }

  /**
    * Creates a Future containing a Flow of JsValue in and out.
    */
  def wsFutureFlow(request: RequestHeader, hash: String): Future[Flow[JsValue, JsValue, NotUsed]] = {
    // create an actor ref source and associated publisher for sink
    val (webSocketOut: ActorRef, webSocketIn: Publisher[JsValue]) = createWebSocketConnections()

    // Create a user actor off the request id and attach it to the source
    val userActorFuture = createUserActor(request.id.toString, webSocketOut, hash)

    // Once we have an actor available, create a flow...
    userActorFuture.map { userActor =>
      createWebSocketFlow(webSocketIn, userActor, hash)
    }
  }

  /**
    * Creates a materialized flow for the websocket, exposing the source and sink.
    *
    * @return the materialized input and output of the flow.
    */
  def createWebSocketConnections(): (ActorRef, Publisher[JsValue]) = {

    // Creates a source to be materialized as an actor reference.
    val source: Source[JsValue, ActorRef] = {
      // If you want to log on a flow, you have to use a logging adapter.
      // http://doc.akka.io/docs/akka/2.4.4/scala/logging.html#SLF4J
      val logging = Logging(system.eventStream, logger.getName)

      // Creating a source can be done through various means, but here we want
      // the source exposed as an actor so we can send it messages from other
      // actors.
      Source.actorRef[JsValue](10, OverflowStrategy.dropTail).log("actorRefSource")(logging)
    }

    // Creates a sink to be materialized as a publisher.  Fanout is false as we only want
    // a single subscriber here.
    val sink: Sink[JsValue, Publisher[JsValue]] = Sink.asPublisher(fanout = false)

    // Connect the source and sink into a flow, telling it to keep the materialized values,
    // and then kicks the flow into existence.
    source.toMat(sink)(Keep.both).run()
  }

  /**
    * Creates a flow of events from the websocket to the user actor.
    *
    * When the flow is terminated, the user actor is no longer needed and is stopped.
    *
    * @param userActor   the user actor receiving websocket events.
    * @param webSocketIn the "read" side of the websocket, that publishes JsValue to UserActor.
    * @return a Flow of JsValue in both directions.
    */
  def createWebSocketFlow(webSocketIn: Publisher[JsValue], userActor: ActorRef, hash: String): Flow[JsValue, JsValue, NotUsed] = {
    // http://doc.akka.io/docs/akka/current/scala/stream/stream-flows-and-basics.html#stream-materialization
    // http://doc.akka.io/docs/akka/current/scala/stream/stream-integrations.html#integrating-with-actors

    // source is what comes in: browser ws events -> play -> publisher -> userActor
    // sink is what comes out:  userActor -> websocketOut -> play -> browser ws events
    val flow = {
      val sink = Sink.actorRef(userActor, akka.actor.Status.Success(()))
      val source = Source.fromPublisher(webSocketIn)
      Flow.fromSinkAndSource(sink, source)
    }

    // Unhook the user actor when the websocket flow terminates
    // http://doc.akka.io/docs/akka/current/scala/stream/stages-overview.html#watchTermination
    val flowWatch: Flow[JsValue, JsValue, NotUsed] = flow.watchTermination() { (_, termination) =>
      termination.foreach { done =>
        logger.info(s"Terminating actor $userActor")
        (dashboardViewActor ? Scodash.Command.FindDashboardByWriteHash(hash)).mapTo[FullResult[List[JObject]]].map {
          result => {
            val dashboardFO = result.value.head.extract[DashboardFO]
            (scodashActor ? (Scodash.Command.FindDashboard(dashboardFO.id))).mapTo[ActorRef].map {
              dashboardActor =>
                dashboardActor ! Dashboard.Command.Unwatch
                system.stop(userActor)
            }
          }
        }
      }
      NotUsed
    }

    flowWatch
  }

  /**
    * Creates a user actor with a given name, using the websocket out actor for output.
    *
    * @param userId         the name of the user actor.
    * @param webSocketOut the "write" side of the websocket, that the user actor sends JsValue to.
    * @return a user actor for this ws connection.
    */
  def createUserActor(userId: String, webSocketOut: ActorRef, hash: String): Future[ActorRef] = {
    val userActorFuture = {
      getDashboard(hash).flatMap{case Some((dashboard, accessMode)) =>
        (scodashActor ? Scodash.Command.CreateDashboardUser(userId, webSocketOut, dashboard.id, accessMode)).mapTo[ActorRef]
      }


    }
    userActorFuture



  }

  def getDashboard(hash: String): Future[Option[(DashboardFO, DashboardAccessMode.Value)]] = {

    var writeFut = scodashActor ? Scodash.Command.FindDashboardByWriteHash(hash)
    var readFut = scodashActor ? Scodash.Command.FindDashboardByReadonlyHash(hash)

    var dashboard = resolveDashboard(writeFut, readFut)
    dashboard.map {
      case Some((dashboard, mode)) => {
        logger.info("Found {} dashboard in maps", hash)
        Some((dashboard, mode))
      }
      case None => {
        logger.info("Not found {} dashboard in maps - going to read model", hash)
        writeFut = dashboardViewActor ? Scodash.Command.FindDashboardByWriteHash(hash)
        readFut = dashboardViewActor ? Scodash.Command.FindDashboardByReadonlyHash(hash)
        Await.result(resolveDashboard(writeFut, readFut), 10 seconds)
      }
    }
  }

  private def resolveDashboard(writeFut: Future[Any], readFut: Future[Any]): Future[Option[(DashboardFO, DashboardAccessMode.Value)]] = {
    for {
      writeDash <- writeFut
      readDash <- readFut
    } yield {
      val maybeReadDashboard = readDash match {
        case readRes: DashboardFO =>
          Some(readRes.removeWriteHash, DashboardAccessMode.READONLY)
        case readRes: FullResult[_] =>
          readRes.value match {
            case List(item) =>
              item match {
                case item: DashboardFO => Some(item.removeWriteHash, DashboardAccessMode.READONLY)
                case jsObject: JObject => Some(jsObject.extract[DashboardFO].removeWriteHash, DashboardAccessMode.READONLY)
              }
            case _ => None
          }
        case _ => None
      }
      writeDash match {
        case writeRes: DashboardFO =>
          Some(writeRes.removeReadOnlyHash, DashboardAccessMode.WRITE)
        case writeRes: FullResult[_] =>
          writeRes.value match {
            case List(item) =>
              item match {
                case dashboard: DashboardFO => Some(dashboard.removeReadOnlyHash, DashboardAccessMode.WRITE)
                case jsObject: JObject => Some(jsObject.extract[DashboardFO].removeReadOnlyHash, DashboardAccessMode.WRITE)
              }
            case _ => None
          }
        case _ => None
      }
    }
  }
}