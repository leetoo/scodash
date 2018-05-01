package controllers


object Forms {

  case class NewDashboard(name: String, description: String) {
    def this() = this("","")
  }

  case class CreateDashboardItems(items: Set[String]) {
    def this(dashboard: Dashboard) = this(dashboard.items)
  }

  case class Item(itemName: String)

  case class CreatedDashboard(name: String, writeHash: String, readOnlyHash: String) {
    def this() = this("","","")
  }

  case class DashboardOwner(ownerName: String, ownerEmail: String, tzOffset: String) {
    def this() = this("","","")
  }

  case class Dashboard(name: String, description: String, items: Set[String], ownerName: String, ownerEmail: String) {

    def this() = this("","", Set(),"","")
    def this(name: String, description: String, style: String) = this(name, description, Set(), "", "")
    def this(name: String, description: String, style: String, items: Set[String]) = this(name, description, items, "", "")

    def updateNameDescStyle(name: String, description: String) = this.copy(name = name, description = description)
    def updateItems(items: Set[String]) = this.copy(items = items)
  }
}
