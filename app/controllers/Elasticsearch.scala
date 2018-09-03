package controllers


import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.typesafe.config.Config
import org.apache.commons.lang3.StringUtils
import org.json4s._
import org.json4s.ext.JodaTimeSerializers
import org.json4s.native.Serialization.{read, write}
import play.api.Logger

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}


object ElasticsearchApi {

  val logger: Logger = Logger(this.getClass)

  trait EsResponse
  case class ShardData(total:Int, failed:Int, successful:Int)
  case class IndexingResult(_shards:ShardData, _index:String, _type:String, _id:String, _version:Int, created:Option[Boolean]) extends EsResponse

  case class UpdateScript(inline:String, params:Map[String,Any])
  case class UpdateRequest(script:UpdateScript)

  case class SearchHit(_source:JObject)
  case class QueryHits(hits:List[SearchHit])
  case class QueryResponse(hits:QueryHits) extends EsResponse

  case class DeleteResult(acknowledged:Boolean) extends EsResponse

  implicit lazy val formats = DefaultFormats ++ JodaTimeSerializers.all
}

trait ElasticsearchSupport { me:AbstractBaseActor =>

  import ElasticsearchApi._

  val timeout = 300.millis

  //implicit val executionContext = context.system.dispatcher

  val esSettings = ElasticsearchSettings(context.system)

  //val ws: WSClient

  val authHeader = Authorization(BasicHttpCredentials(esSettings.username, esSettings.password))

  def indexRoot:String

  def entityType:String

  def baseUrl = s"${esSettings.rootUrl}/${indexRoot}/$entityType"


  //final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()


  def queryElasticsearch(query:String)(implicit ec:ExecutionContext):Future[List[JObject]] = {

    //val req = url(s"$baseUrl/_search") <<? Map("q" -> query)

    //val req = HttpRequest(uri = s"$baseUrl/_search").with // TODO q
    val req = HttpRequest().withUri(Uri(s"$baseUrl/_search").withQuery(Query(Map("q" -> query))))
    callElasticsearch[QueryResponse](req).
      map(_.hits.hits.map(_._source))
  }

  def updateIndex(id:String, request:AnyRef, version:Option[Long])(implicit ec:ExecutionContext):Future[IndexingResult] = {
    val urlBase = s"$baseUrl/$id"
    val requestUrl = version match{
      case None => urlBase
      case Some(v) => s"$urlBase/_update?version=$v"
    }
    val req = HttpRequest(uri = requestUrl, method = HttpMethods.PUT, entity = HttpEntity(write(request)).withContentType(ContentTypes.`application/json`))
    //req.setContentType("application/json", "UTF-8")
    //req.setBody(write(request))
    //req.setMethod("PUT")
    callElasticsearch[IndexingResult](req)

  }

  def clearIndex(implicit ec:ExecutionContext) = {
    val req = HttpRequest(uri = s"${esSettings.rootUrl}/${indexRoot}/", method = HttpMethods.DELETE)
    callElasticsearch[DeleteResult](req)
  }

  def callElasticsearch[RT : Manifest](req:HttpRequest)(implicit ec:ExecutionContext):Future[RT] = {

    val reqHeaders = req.withHeaders(List(authHeader))
    req.withEntity(req.entity withContentType(ContentTypes.`application/json`))

    val respFut: Future[HttpResponse] = Http(context.system).singleRequest(reqHeaders)


    respFut.flatMap[RT] { /*resp: HttpResponse => match {*/
      case response @ HttpResponse(StatusCodes.OK, headers, entity, _) =>
        processEntity(entity)
      case response @ HttpResponse(StatusCodes.Created, headers, entity, _) =>
        processEntity(entity)
      case response @ HttpResponse(StatusCodes.Accepted, headers, entity, _) =>
        processEntity(entity)
      case resp @ HttpResponse(code, _, _, _) =>
        Future.failed[RT](new IllegalStateException(s"Unexpected HTTP status ${code}"))
    }

      //Future(read(resp.entity.toString))}
//    respFut.map(resp => {
//      resp.entity(as[RT]) {
//        complete
//      }
//    })
//    respFut
//      .onComplete {
//        case scala.util.Success(res) => {
//          val body = res.entity.dataBytes.map[String](byteString => byteString.utf8String)
//          read[RT](body)
//        }
//        case scala.util.Failure(_)   => {
//          sys.error("something wrong")
//        }
//      }





    //Http(req as(esSettings.username, esSettings.password) OK as.String).map(resp => read[RT](resp))
  }

  private def processEntity[RT: Manifest](entity: ResponseEntity)(implicit ec:ExecutionContext) = {
    val body: Future[String] = entity.toStrict(timeout)(ActorMaterializer(ActorMaterializerSettings(context.system))).map(_.data).map(_.utf8String)
    body.flatMap[RT] { body: String => Future(read[RT](body)) }
  }
}

class ElasticsearchSettingsImpl(conf:Config) extends Extension{
  val esConfig = conf.getConfig("elasticsearch")
  val protocol = esConfig.getString("protocol")
  val host = esConfig.getString("host")
  val port = esConfig.getString("port")
  val rootUrl = if (StringUtils.isNotBlank(port)) s"$protocol://$host:$port" else s"$protocol://$host"
  val username = esConfig.getString("username")
  val password = esConfig.getString("password")
}

object ElasticsearchSettings extends ExtensionId[ElasticsearchSettingsImpl] with ExtensionIdProvider {
  override def lookup = ElasticsearchSettings
  override def createExtension(system: ExtendedActorSystem) =
    new ElasticsearchSettingsImpl(system.settings.config)
}