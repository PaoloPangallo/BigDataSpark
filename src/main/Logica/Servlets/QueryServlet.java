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
        LOGGER.info("QueryServlet inizializzato con directory: " + outputDirectory);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String type = req.getParameter("type");
        String filename = req.getParameter("filename");

        LOGGER.info("Ricevuta richiesta GET per QueryServlet.");

        try {
            if ("statistics".equals(type)) {
                // Gestione della richiesta per le statistiche
                LOGGER.info("Richiesta per il file delle statistiche.");
                if (!dataService.fileExists("chessStatistics.csv")) {
                    ServletUtils.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "File delle statistiche non trovato.");
                    return;
                }

                String content = dataService.getFileContent("chessStatistics.csv"); // File fisso per le statistiche
                String json = ServletUtils.csvToJson(content); // Converte il CSV in JSON
                ServletUtils.sendResponse(resp, json, "application/json");

            } else if (filename != null && !filename.trim().isEmpty()) {
                // Gestione della richiesta per file specifici
                LOGGER.info("Richiesta per il file: " + filename);
                if (!dataService.fileExists(filename)) {
                    ServletUtils.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "File non trovato: " + filename);
                    return;
                }

                String content = dataService.getFileContent(filename);
                ServletUtils.sendResponse(resp, content, "text/csv");

            } else {
                // Parametri mancanti o non validi
                LOGGER.warning("Parametro 'filename' o 'type' richiesto ma non fornito.");
                ServletUtils.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Parametro 'filename' o 'type' richiesto.");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore nel recupero del file: " + e.getMessage(), e);
            ServletUtils.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'elaborazione della richiesta.");
        }
    }
}
