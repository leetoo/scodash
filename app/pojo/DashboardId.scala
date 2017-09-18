package pojo

import java.util.UUID

import io.circe.{Decoder, Encoder}


class DashboardId(val id: UUID) extends AnyVal {
  override def toString: String = id.toString
}

object DashboardId {
  def apply(): DashboardId = new DashboardId(UUID.randomUUID())

  def apply(id: UUID): DashboardId = new DashboardId(id)

  implicit val postIdDecoder: Decoder[DashboardId] =
    Decoder.decodeUUID.map(DashboardId(_))
  implicit val postIdEncoder: Encoder[DashboardId] =
    Encoder.encodeUUID.contramap(_.id)
}