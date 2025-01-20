package Servlets;


import jakarta.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import java.io.File
import scala.io.Source

class ResultCountsServlet(outputDirectory: String) extends HttpServlet {
    override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
        val filePath = s"$outputDirectory\\resultCounts\\resultCounts.json"
        serveJsonFile(filePath, resp)
    }

    private def serveJsonFile(filePath: String, resp: HttpServletResponse): Unit = {
        val file = new File(filePath)
        if (!file.exists()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND)
            resp.getWriter.write(s"""{"error": "File non trovato: $filePath"}""")
            return
        }

        try {
            val data = Source.fromFile(file).getLines().mkString
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

