package Utils

import jakarta.servlet.http.HttpServletResponse
import scala.io.Source
import java.io.File

object JsonUtils {
  def serveJson(filePath: String, resp: HttpServletResponse): Unit = {
    val file = new File(filePath)
    if (!file.exists()) {
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND)
      resp.getWriter.write(s"""{"error": "File non trovato: $filePath"}""")
      return
    }

    try {
      val data = Source.fromFile(file).mkString
      resp.setContentType("application/json")
      resp.setStatus(HttpServletResponse.SC_OK)
      resp.getWriter.write(data)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
        resp.getWriter.write(s"""{"error": "Errore durante la lettura del file: $filePath"}""")
    }
  }
}
