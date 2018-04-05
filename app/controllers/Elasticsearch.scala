package controllers


import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import com.typesafe.config.Config
import org.apache.commons.lang3.StringUtils
import org.json4s._
import org.json4s.ext.JodaTimeSerializers
import org.json4s.native.Serialization.{read, write}
import play.api.Logger

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

trait ElasticsearchSupport{ me:AbstractBaseActor =>
  import ElasticsearchApi._

  //implicit val executionContext = context.system.dispatcher

  val esSettings = ElasticsearchSettings(context.system)

  val authHeader = Authorization(BasicHttpCredentials(esSettings.username, esSettings.password))

  def indexRoot:String

  def entityType:String

  def baseUrl = s"${esSettings.rootUrl}/${indexRoot}/$entityType"

  def queryElasticsearch(query:String)(implicit ec:ExecutionContext):Future[List[JObject]] = {

    //val req = url(s"$baseUrl/_search") <<? Map("q" -> query)
    val req = HttpRequest(uri = s"$baseUrl/_search") // TODO q
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

    //read("xx")

    val reqHeaders = req.withHeaders(List(authHeader))
    req.withEntity(req.entity withContentType(ContentTypes.`application/json`))

    val respFut: Future[HttpResponse] = Http(context.system).singleRequest(reqHeaders)
    respFut.flatMap[RT]{resp:HttpResponse => Future(read(resp.entity.toString))}
//    respFut.map(resp => {
//      resp.entity(as[RT]) {
//        complete
//      }
//    })
//    respFut
//      .onComplete {
//        case Success(res) => println(res)
//        case Failure(_)   => sys.error("something wrong")
//      }



    //Http(req as(esSettings.username, esSettings.password) OK as.String).map(resp => read[RT](resp))
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