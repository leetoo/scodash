package controllers

import akka.actor._
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}

import scala.concurrent.{ExecutionContext, Future}

import ExecutionContext.Implicits.global
/**
  * Interface into a projection's offset storage system so that it can be properly resumed
  */
abstract class ResumableProjection(identifier:String) {
  def storeLatestOffset(offset:Long):Future[Boolean]
  def fetchLatestOffset:Future[Option[Long]]
}

object ResumableProjection {

  def apply(identifier:String, system:ActorSystem) =
    new PostgresResumableProjection(identifier, system)
}

class PostgresResumableProjection(identifier:String, system:ActorSystem) extends ResumableProjection(identifier){
  val projectionStorage = PostgresProjectionStorage(system)

  def storeLatestOffset(offset:Long):Future[Boolean] = {
    projectionStorage.updateOffset(identifier, offset + 1)
  }

  def fetchLatestOffset:Future[Option[Long]] = {
    projectionStorage.fetchLatestOffset(identifier)
  }
}

class PostgresProjectionStorageExt(system:ActorSystem, offsetStore: OffsetStore) extends Extension {

  def updateOffset(identifier:String, offset:Long): Future[Boolean] = {
    offsetStore.save(identifier, offset) map { _ => true }
  }

  def fetchLatestOffset(identifier:String): Future[Option[Long]] = {
    offsetStore.load(identifier) map  { res => Option(res)}
  }
}
object PostgresProjectionStorage extends ExtensionId[PostgresProjectionStorageExt] with ExtensionIdProvider {

  val dataSource: HikariDataSource = {
    val config = new HikariConfig()
    config.setJdbcUrl(sys.env("SLICK_DB_URL"))
    config.setUsername(sys.env("SLICK_DB_USER"))
    config.setPassword(sys.env("SLICK_DB_PASSWORD"))
    config.setDriverClassName("org.postgresql.Driver")
    config.setMaximumPoolSize(1);
    new HikariDataSource(config)
  }

  override def lookup = PostgresProjectionStorage
  override def createExtension(system: ExtendedActorSystem) =
    new PostgresProjectionStorageExt(system, new PostgresOffsetStore(dataSource))
}