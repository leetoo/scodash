package controllers

object Forms {

  case class NewDashboard(name: String, description: String, style: String) {
    def this() = this("","","")
  }

  case class CreateDashboardItems(items: List[String])

  case class CreatedDashboard(name: String, writeHash: String, readOnlyHash: String) {
    def this() = this("","","")
  }

  case class DashboardOwner(name: String, email: String) {
    def this() = this("","")
  }
}
