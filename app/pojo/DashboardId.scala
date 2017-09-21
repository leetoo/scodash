package pojo

import io.circe.{Decoder, Encoder}

class DashboardId(val writeHash: String) extends AnyVal

object DashboardId {

  def apply(writeHash: String): DashboardId = new DashboardId(writeHash)


}