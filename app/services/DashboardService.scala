package services

import pojo.{Dashboard, DashboardId}

trait DashboardService {
  def getDashboard(id: DashboardId)
  def addDashboard(content: Dashboard)

}
