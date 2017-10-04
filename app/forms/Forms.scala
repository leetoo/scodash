package forms

object Forms {
  case class CreatedDashboard(name: String = "", writeHash: String = "", readOnlyHash: String = "") {
    def this() = this("","","")
  }
}
