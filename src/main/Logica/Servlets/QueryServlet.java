package Servlets;

import Services.DataService;
import Utils.ServletUtils;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueryServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(QueryServlet.class.getName());
    private DataService dataService;

    @Override
    public void init() {
        String outputDirectory = getServletContext().getInitParameter("outputDirectory");
        if (outputDirectory == null || outputDirectory.isEmpty()) {
            throw new IllegalStateException("Parametro 'outputDirectory' non configurato o nullo.");
        }
        dataService = new DataService(outputDirectory);
        LOGGER.info("AvgRatingDiffServlet inizializzato con directory: " + outputDirectory);
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String filename = req.getParameter("filename");
        LOGGER.info("Ricevuta richiesta GET per AvgRatingDiffServlet.");

        try {
            String content = dataService.getFileContent(filename);
            ServletUtils.sendResponse(resp, content, "text/csv");
            LOGGER.info("Risposta inviata con successo per AvgRatingDiffServlet.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore nel recupero del file avgRatingDiff.csv: " + e.getMessage(), e);
            ServletUtils.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "File non trovato: avgRatingDiff.csv");
        }
    }
}
