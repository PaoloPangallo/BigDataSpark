import Servlets.QueryServlet;

import Servlets.RootServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.logging.Logger;

public class ChessServer {

    private static final Logger LOGGER = Logger.getLogger(ChessServer.class.getName());
    private static final String OUTPUT_DIRECTORY = "C:/Users/paolo/Desktop/ProgettoSparkScacchi";
    private static final String WEB_APP_DIRECTORY = "src/main/webapp"; // Percorso dei file statici
    private static final int SERVER_PORT = 8088;

    public static void main(String[] args) {
        try {
            Server server = new Server(SERVER_PORT);

            // Handler per i file statici
            ResourceHandler staticHandler = new ResourceHandler();
            staticHandler.setResourceBase(WEB_APP_DIRECTORY);
            staticHandler.setWelcomeFiles(new String[]{"index.html"});
            staticHandler.setDirectoriesListed(false);

            // Configura il contesto dei servlet
            ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
            servletContext.setContextPath("/");

            // Aggiungi parametro di contesto globale
            servletContext.setInitParameter("outputDirectory", OUTPUT_DIRECTORY);

            // Aggiungi servlet
          /*  servletContext.addServlet(ResultCountsServlet.class, "/resultCounts");
            servletContext.addServlet(PopularOpeningsServlet.class, "/popularOpenings");*/
            servletContext.addServlet(QueryServlet.class, "/avgRatingDiff");
            servletContext.addServlet(RootServlet.class, "/");

            // Combina gestori statici e servlet
            HandlerCollection handlerCollection = new HandlerCollection();
            handlerCollection.addHandler(staticHandler);
            handlerCollection.addHandler(servletContext);

            server.setHandler(handlerCollection);

            // Avvia il server
            server.start();
            LOGGER.info("Server avviato su http://localhost:" + SERVER_PORT);
            server.join();
        } catch (Exception e) {
            LOGGER.severe("Errore durante l'avvio del server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
