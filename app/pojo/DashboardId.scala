package pojo

import java.util.UUID

import io.circe.{Decoder, Encoder}

class DashboardId(val writeHash: String) extends AnyVal

object DashboardId {

  def apply(writeHash: String): DashboardId = new DashboardId(writeHash)

  // custom circe marshaller and unmarshaller so that the id is not wrapped
  implicit val postIdDecoder: Decoder[DashboardId] = Decoder.decodeString.map(DashboardId(_))
  implicit val postIdEncoder: Encoder[DashboardId] = Encoder.encodeString.contramap(_.writeHash)

}