import Servlets.AvgRatingDiffServlet;
import Servlets.PopularOpeningsServlet;
import Servlets.ResultCountsServlet;
import Servlets.RootServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.server.handler.ResourceHandler;

public class ChessServer {

    private static final String OUTPUT_DIRECTORY = "C:\\Users\\paolo\\Desktop\\ProgettoSparkScacchi\\Output";
    private static final String WEB_APP_DIRECTORY = "webapp"; // Directory contenente i file statici

    public static void main(String[] args) throws Exception {

        // Configura il server Jetty
        Server server = new Server(8080); // Porta 8080

        // Crea un gestore per i file statici (ad esempio, index.html, CSS, JS)
        ResourceHandler staticHandler = new ResourceHandler();
        staticHandler.setResourceBase(WEB_APP_DIRECTORY);
        staticHandler.setDirectoriesListed(true); // Impostazione per l'accesso ai file nelle directory

        // Crea il contesto per i servlet dinamici
        ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContext.setContextPath("/"); // Context root

        // Aggiungi i servlet con il parametro outputDirectory
        servletContext.addServlet(new ServletHolder(new RootServlet()), "/"); // Root servlet
        servletContext.addServlet(new ServletHolder(new ResultCountsServlet(OUTPUT_DIRECTORY)), "/resultCounts"); // Endpoint ResultCounts
        servletContext.addServlet(new ServletHolder(new PopularOpeningsServlet(OUTPUT_DIRECTORY)), "/popularOpenings"); // Endpoint PopularOpenings
        servletContext.addServlet(new ServletHolder(new AvgRatingDiffServlet(OUTPUT_DIRECTORY)), "/avgRatingDiff"); // Endpoint AvgRatingDiff

        // Combina il gestore statico con il contesto dei servlet dinamici
        HandlerCollection handlerCollection = new HandlerCollection();
        handlerCollection.setHandlers(new org.eclipse.jetty.server.Handler[] { staticHandler, servletContext });

        server.setHandler(handlerCollection);

        // Avvia il server
        server.start();
        System.out.println("Server avviato su http://localhost:8080");
        System.out.println("File statici disponibili dal percorso: /");
        System.out.println("Endpoint disponibili:");
        System.out.println("1. Vittorie per colore: http://localhost:8080/resultCounts");
        System.out.println("2. Aperture popolari: http://localhost:8080/popularOpenings");
        System.out.println("3. Differenze medie di rating: http://localhost:8080/avgRatingDiff");

        server.join();
    }
}
