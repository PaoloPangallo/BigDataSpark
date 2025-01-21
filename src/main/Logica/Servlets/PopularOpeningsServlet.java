package Servlets;

import Services.DataService;
import Utils.ServletUtils;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PopularOpeningsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PopularOpeningsServlet.class.getName());
    private DataService dataService;

    @Override
    public void init() {
        try {
            String outputDirectory = getServletContext().getInitParameter("outputDirectory");
            if (outputDirectory == null || outputDirectory.isEmpty()) {
                throw new IllegalStateException("Parametro 'outputDirectory' non configurato o nullo.");
            }
            dataService = new DataService(outputDirectory);
            LOGGER.info("PopularOpeningsServlet inizializzato con directory: " + outputDirectory);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'inizializzazione del servlet: " + e.getMessage(), e);
            throw new RuntimeException("Errore nell'inizializzazione del servlet PopularOpeningsServlet.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LOGGER.info("Ricevuta richiesta GET per PopularOpeningsServlet.");
        try {
            String content = dataService.getFileContent("popularOpenings.csv");
            ServletUtils.sendResponse(resp, content, "text/csv");
            LOGGER.info("Risposta inviata con successo per PopularOpeningsServlet.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore nel recupero del file popularOpenings.csv: " + e.getMessage(), e);
            ServletUtils.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "File non trovato: popularOpenings.csv");
        }
    }
}
