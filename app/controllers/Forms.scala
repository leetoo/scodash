package controllers

object Forms {
  case class CreatedDashboard(name: String, writeHash: String, readOnlyHash: String) {
    def this() = this("","","")
  }
  case class DashboardOwner(name: String, email: String) {
    def this() = this("","")
  }
}
