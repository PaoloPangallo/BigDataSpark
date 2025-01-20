package Logica

import org.eclipse.jetty.server.{Server, Handler}
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.handler.ContextHandlerCollection
import org.eclipse.jetty.server.handler.ContextHandler
import jakarta.servlet.http.HttpServlet
import java.io.File

object ChessServer {

  private val OutputDirectory = "C:\\Users\\paolo\\Desktop\\ProgettoSparkScacchi\\Output"
  private val WebAppDirectory = "webapp" // Directory contenente i file statici

  def main(args: Array[String]): Unit = {

    // Configura il server Jetty
    val server = new Server(8080) // Porta 8080

    // Crea un gestore per i file statici (ad esempio, index.html, CSS, JS)
    val staticHandler = new ResourceHandler()
    staticHandler.setResourceBase(WebAppDirectory)
    staticHandler.setDirectoriesListed(true) // Impostazione per l'accesso ai file nelle directory

    // Crea il contesto per i servlet dinamici
    val servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS)
    servletContext.setContextPath("/") // Context root
    servletContext.addServlet(new ServletHolder(new RootServlet()), "/") // Root servlet
    servletContext.addServlet(new ServletHolder(new ResultCountsServlet()), "/resultCounts") // Endpoint ResultCounts
    servletContext.addServlet(new ServletHolder(new PopularOpeningsServlet()), "/popularOpenings") // Endpoint PopularOpenings
    servletContext.addServlet(new ServletHolder(new AvgRatingDiffServlet()), "/avgRatingDiff") // Endpoint AvgRatingDiff

    // Combina il gestore statico con il contesto dei servlet dinamici
    val handlerCollection = new HandlerCollection()
    handlerCollection.setHandlers(Array(staticHandler, servletContext))

    server.setHandler(handlerCollection)

    // Avvia il server
    server.start()
    println("Server avviato su http://localhost:8080")
    println("File statici disponibili dal percorso: /")
    println("Endpoint disponibili:")
    println("1. Vittorie per colore: http://localhost:8080/resultCounts")
    println("2. Aperture popolari: http://localhost:8080/popularOpenings")
    println("3. Differenze medie di rating: http://localhost:8080/avgRatingDiff")

    server.join()
  }

  // Servlet per la root
  class RootServlet extends HttpServlet {
    override def doGet(req: jakarta.servlet.http.HttpServletRequest, resp: jakarta.servlet.http.HttpServletResponse): Unit = {
      resp.setContentType("text/html")
      resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_OK)
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

  // Servlet per i conteggi delle vittorie
  class ResultCountsServlet extends HttpServlet {
    override def doGet(req: jakarta.servlet.http.HttpServletRequest, resp: jakarta.servlet.http.HttpServletResponse): Unit = {
      val filePath = s"$OutputDirectory\\resultCounts\\resultCounts.json"
      serveJsonFile(filePath, resp)
    }
  }

  // Servlet per le aperture più popolari
  class PopularOpeningsServlet extends HttpServlet {
    override def doGet(req: jakarta.servlet.http.HttpServletRequest, resp: jakarta.servlet.http.HttpServletResponse): Unit = {
      val filePath = s"$OutputDirectory\\popularOpenings\\popularOpenings.json"
      serveJsonFile(filePath, resp)
    }
  }

  // Servlet per la differenza media del rating
  class AvgRatingDiffServlet extends HttpServlet {
    override def doGet(req: jakarta.servlet.http.HttpServletRequest, resp: jakarta.servlet.http.HttpServletResponse): Unit = {
      val filePath = s"$OutputDirectory\\avgRatingDiff\\avgRatingDiff.json"
      serveJsonFile(filePath, resp)
    }
  }

  // Metodo per leggere e servire un file JSON
  private def serveJsonFile(filePath: String, resp: jakarta.servlet.http.HttpServletResponse): Unit = {
    val file = new File(filePath)
    if (!file.exists()) {
      resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND)
      resp.getWriter.write(s"""{"error": "File non trovato: $filePath"}""")
      return
    }

    try {
      val data = scala.io.Source.fromFile(file).getLines().mkString
      resp.setContentType("application/json")
      resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_OK)
      resp.getWriter.write(data)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
        resp.getWriter.write(s"""{"error": "Errore durante la lettura del file: $filePath"}""")
    }
  }
}
