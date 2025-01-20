package Servlets

import Utils.JsonUtils.serveJson
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}


class AvgRatingDiffServlet(outputDirectory: String) extends ResultCountsServlet(outputDirectory) {
  override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    val filePath = s"$outputDirectory\\avgRatingDiff\\avgRatingDiff.json"
    serveJson(filePath, resp)
  }
}

