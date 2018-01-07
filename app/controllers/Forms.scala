package controllers

import play.data.validation.Constraints


object Forms {

  case class NewDashboard(name: String, description: String, style: String) {
    def this() = this("","","")
  }

  case class CreateDashboardItems(items: Set[String]) {
    def this(dashboard: Dashboard) = this(dashboard.items)
  }

  case class Item(itemName: String)

  case class CreatedDashboard(name: String, writeHash: String, readOnlyHash: String) {
    def this() = this("","","")
  }

  case class DashboardOwner(ownerName: String, ownerEmail: String) {
    def this() = this("","")
  }

  case class Dashboard(name: String, description: String, style: String, items: Set[String], ownerName: String, ownerEmail: String) {

    def this() = this("","","", Set(),"","")
    def this(name: String, description: String, style: String) = this(name, description, style, Set(), "", "")
    def this(name: String, description: String, style: String, items: Set[String]) = this(name, description, style, items, "", "")

    def updateNameDescStyle(name: String, description: String, style: String) = this.copy(name = name, description = description, style = style)
    def updateItems(items: Set[String]) = this.copy(items = items)
  }
}
