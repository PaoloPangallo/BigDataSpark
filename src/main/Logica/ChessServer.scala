package Logica

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}
import jakarta.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import scala.io.Source
import java.io.File

object ChessServer {
  private val OutputDirectory = "C:\\Users\\paolo\\Desktop\\ProgettoSparkScacchi\\Output"

  def main(args: Array[String]): Unit = {
    // Configura il server Jetty
    val server = new Server(8080) // Porta 8080
    val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
    context.setContextPath("/") // Context root
    server.setHandler(context)

    // Aggiungi i servlet per servire i dati
    context.addServlet(new ServletHolder(classOf[RootServlet]), "/") // Root
    context.addServlet(new ServletHolder(classOf[ResultCountsServlet]), "/resultCounts") // Vittorie
    context.addServlet(new ServletHolder(classOf[PopularOpeningsServlet]), "/popularOpenings") // Aperture
    context.addServlet(new ServletHolder(classOf[AvgRatingDiffServlet]), "/avgRatingDiff") // Differenze rating

    // Avvia il server
    server.start()
    println("Server avviato su http://localhost:8080")
    println("Endpoint disponibili:")
    println("1. Vittorie per colore: http://localhost:8080/resultCounts")
    println("2. Aperture popolari: http://localhost:8080/popularOpenings")
    println("3. Differenze medie di rating: http://localhost:8080/avgRatingDiff")

    server.join()
  }

  // Servlet per la root
  class RootServlet extends HttpServlet {
    override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
      resp.setContentType("text/html")
      resp.setStatus(HttpServletResponse.SC_OK)
      resp.getWriter.write(
        s"""
           |<html>
           |<body>
           |<h1>Benvenuto nel server di analisi scacchi</h1>
           |<p>Usa uno dei seguenti endpoint per accedere ai dati:</p>
           |<ul>
           |  <li><a href="/resultCounts">Vittorie per colore</a></li>
           |  <li><a href="/popularOpenings">Aperture più popolari</a></li>
           |  <li><a href="/avgRatingDiff">Differenze medie di rating</a></li>
           |</ul>
           |</body>
           |</html>
         """.stripMargin)
    }
  }

  // Servlet per servire i conteggi delle vittorie
  class ResultCountsServlet extends HttpServlet {
    override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
      val filePath = s"$OutputDirectory\\resultCounts\\resultCounts.json"
      serveJsonFile(filePath, resp)
    }
  }

  class PopularOpeningsServlet extends HttpServlet {
    override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
      val filePath = s"$OutputDirectory\\popularOpenings\\popularOpenings.json"
      serveJsonFile(filePath, resp)
    }
  }

  class AvgRatingDiffServlet extends HttpServlet {
    override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
      val filePath = s"$OutputDirectory\\avgRatingDiff\\avgRatingDiff.json"
      serveJsonFile(filePath, resp)
    }
  }


  // Metodo ausiliario per leggere e servire un file JSON
  private def serveJsonFile(filePath: String, resp: HttpServletResponse): Unit = {
    val file = new File(filePath)
    if (!file.exists()) {
      // Gestione dell'errore - File non trovato
      println(s"Errore: File non trovato: $filePath")
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND)
      resp.getWriter.write(s"""{"error": "File non trovato: $filePath"}""")
      return
    }

    try {
      // Leggi il contenuto del file JSON
      val data = Source.fromFile(file).getLines().mkString
      resp.setContentType("application/json")
      resp.setStatus(HttpServletResponse.SC_OK)
      resp.getWriter.write(data)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        println(s"Errore durante la lettura del file: $filePath")
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
        resp.getWriter.write(s"""{"error": "Errore durante la lettura del file: $filePath"}""")
    }
  }
}
