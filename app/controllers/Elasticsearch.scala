package controllers


import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.typesafe.config.Config
import dispatch._
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

  val esSettings = ElasticsearchSettings(context.system)

  def indexRoot:String

  def entityType:String

  def baseUrl = s"${esSettings.rootUrl}/${indexRoot}/$entityType"

  def queryElasticsearch(query:String)(implicit ec:ExecutionContext):Future[List[JObject]] = {

    logger.debug(s"$baseUrl/_search")
    logger.debug(s"q:$query")

    val req = url(s"$baseUrl/_search") <<? Map("q" -> query)

    callElasticsearch[QueryResponse](req).
      map(_.hits.hits.map(_._source))
  }

  def updateIndex(id:String, request:AnyRef, version:Option[Long])(implicit ec:ExecutionContext):Future[IndexingResult] = {
    val urlBase = s"$baseUrl/$id"
    val requestUrl = version match{
      case None => urlBase
      case Some(v) => s"$urlBase/_update?version=$v"
    }
    val req = url(requestUrl) << write(request)
    callElasticsearch[IndexingResult](req)
  }

  def clearIndex(implicit ec:ExecutionContext) = {
    val req = url(s"${esSettings.rootUrl}/${indexRoot}/").DELETE
    callElasticsearch[DeleteResult](req)
  }

  def callElasticsearch[RT : Manifest](req:Req)(implicit ec:ExecutionContext):Future[RT] = {
    Http(req OK as.String).map(resp => read[RT](resp))
  }
}

class ElasticsearchSettingsImpl(conf:Config) extends Extension{
  val esConfig = conf.getConfig("elasticsearch")
  val protocol = esConfig.getString("protocol")
  val host = esConfig.getString("host")
  val port = esConfig.getInt("port")
  val rootUrl = s"$protocol://$host:$port"
}
object ElasticsearchSettings extends ExtensionId[ElasticsearchSettingsImpl] with ExtensionIdProvider {
  override def lookup = ElasticsearchSettings
  override def createExtension(system: ExtendedActorSystem) =
    new ElasticsearchSettingsImpl(system.settings.config)
}