import Servlets.AvgRatingDiffServlet;
import Servlets.PopularOpeningsServlet;
import Servlets.ResultCountsServlet;
import Servlets.RootServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class ChessServer {

    private static final String OUTPUT_DIRECTORY = "C:/Users/paolo/Desktop/ProgettoSparkScacchi/Output";
    private static final String WEB_APP_DIRECTORY = "src/main/webapp"; // Percorso dei file statici

    public static void main(String[] args) throws Exception {

        Server server = new Server(8080);

        // Handler per i file statici
        ResourceHandler staticHandler = new ResourceHandler();
        staticHandler.setResourceBase(WEB_APP_DIRECTORY);
        staticHandler.setWelcomeFiles(new String[]{"index.html"});
        staticHandler.setDirectoriesListed(false);

        // Handler per i servlet
        ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContext.setContextPath("/");

        // Configura i servlet
        ServletHolder resultCountsHolder = new ServletHolder(new ResultCountsServlet());
        resultCountsHolder.setInitParameter("outputDirectory", OUTPUT_DIRECTORY);
        servletContext.addServlet(resultCountsHolder, "/resultCounts");

        ServletHolder popularOpeningsHolder = new ServletHolder(new PopularOpeningsServlet());
        popularOpeningsHolder.setInitParameter("outputDirectory", OUTPUT_DIRECTORY);
        servletContext.addServlet(popularOpeningsHolder, "/popularOpenings");

        ServletHolder avgRatingDiffHolder = new ServletHolder(new AvgRatingDiffServlet());
        avgRatingDiffHolder.setInitParameter("outputDirectory", OUTPUT_DIRECTORY);
        servletContext.addServlet(avgRatingDiffHolder, "/avgRatingDiff");

        ServletHolder rootServletHolder = new ServletHolder(new RootServlet());
        rootServletHolder.setInitParameter("webAppDirectory", WEB_APP_DIRECTORY);
        servletContext.addServlet(rootServletHolder, "/");

        // Combina i gestori statici e servlet
        HandlerCollection handlerCollection = new HandlerCollection();
        handlerCollection.addHandler(staticHandler);
        handlerCollection.addHandler(servletContext);

        server.setHandler(handlerCollection);

        // Avvia il server
        server.start();
        System.out.println("Server avviato su http://localhost:8080");
        server.join();
    }
}
