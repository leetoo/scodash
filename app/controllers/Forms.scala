package controllers

object Forms {

  case class NewDashboard(name: String, description: String, style: String) {
    def this() = this("","","")
  }

  case class CreateDashboardItems(items: List[String])

  case class Item(itemName: String)

  case class CreatedDashboard(name: String, writeHash: String, readOnlyHash: String) {
    def this() = this("","","")
  }

  case class DashboardOwner(ownerName: String, ownerEmail: String) {
    def this() = this("","")
  }

  case class Dashboard(name: String, description: String, style: String, items: List[String], ownerName: String, ownerEmail: String) {
    def this(name: String, description: String, style: String) = this(name, description, style, null, "", "")
    def this(name: String, description: String, style: String, items: List[String]) = this(name, description, style, items, "", "")
    def setItems(items: List[String]) = this.copy(items = items)
  }
}
