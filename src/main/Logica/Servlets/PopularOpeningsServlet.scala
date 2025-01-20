package Servlets

import Utils.JsonUtils.serveJson
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}


class PopularOpeningsServlet(outputDirectory: String) extends ResultCountsServlet(outputDirectory) {
  override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    val filePath = s"$outputDirectory\\popularOpenings\\popularOpenings.json"
    serveJson(filePath, resp)
  }
}

