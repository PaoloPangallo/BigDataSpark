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
                handleStatisticsRequest(resp);
            } else if ("mvp".equals(type)) {
                handleMVPRequest(resp);
            } else if ("dateStatistics".equals(type)) {
                handleDateStatisticsRequest(resp);
            } else if ("specificQueries".equals(type)) { // Nuovo tipo per gestire query specifiche
                handleSpecificQueryRequest(resp);
            } else if (filename != null && !filename.trim().isEmpty()) {
                handleFileRequest(filename, resp);
            } else {
                LOGGER.warning("Parametro 'filename' o 'type' richiesto ma non fornito.");
                ServletUtils.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Parametro 'filename' o 'type' richiesto.");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore nel recupero del file: " + e.getMessage(), e);
            ServletUtils.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'elaborazione della richiesta.");
        }
    }

    private void handleStatisticsRequest(HttpServletResponse resp) throws IOException {
        LOGGER.info("Richiesta per il file delle statistiche generali.");
        if (dataService.fileExists("chessStatistics.csv")) {
            ServletUtils.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "File delle statistiche non trovato.");
            return;
        }

        String content = dataService.getFileContent("chessStatistics.csv");
        String json = ServletUtils.csvToJson(content);
        ServletUtils.sendResponse(resp, json, "application/json");
    }

    private void handleMVPRequest(HttpServletResponse resp) throws IOException {
        LOGGER.info("Richiesta per il file delle statistiche MVP.");
        if (dataService.fileExists("topPlayersAbsolute.csv") ||
                dataService.fileExists("topPlayersPercentage.csv") ||
                dataService.fileExists("topWinningOpenings.csv") ||
                dataService.fileExists("topTournaments.csv")) {

            ServletUtils.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Uno o più file delle statistiche MVP non trovato.");
            return;
        }

        String playersAbsolute = dataService.getFileContent("topPlayersAbsolute.csv");
        String playersPercentage = dataService.getFileContent("topPlayersPercentage.csv");
        String openings = dataService.getFileContent("topWinningOpenings.csv");
        String tournaments = dataService.getFileContent("topTournaments.csv");

        String jsonResponse = "{"
                + "\"topPlayersAbsolute\": " + ServletUtils.csvToJson(playersAbsolute) + ","
                + "\"topPlayersPercentage\": " + ServletUtils.csvToJson(playersPercentage) + ","
                + "\"topWinningOpenings\": " + ServletUtils.csvToJson(openings) + ","
                + "\"topTournaments\": " + ServletUtils.csvToJson(tournaments)
                + "}";

        ServletUtils.sendResponse(resp, jsonResponse, "application/json");
    }

    private void handleDateStatisticsRequest(HttpServletResponse resp) throws IOException {
        LOGGER.info("Richiesta per il file delle statistiche delle date.");
        if (dataService.fileExists("mostGamesDay.csv") ||
                dataService.fileExists("mostGamesForOpeningDay.csv") ||
                dataService.fileExists("dayWithMostUniqueOpenings.csv")) {

            ServletUtils.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Uno o più file delle statistiche delle date non trovato.");
            return;
        }

        String mostGamesDay = dataService.getFileContent("mostGamesDay.csv");
        String mostGamesForOpeningDay = dataService.getFileContent("mostGamesForOpeningDay.csv");
        String dayWithMostUniqueOpenings = dataService.getFileContent("dayWithMostUniqueOpenings.csv");

        String jsonResponse = "{"
                + "\"mostGamesDay\": " + ServletUtils.csvToJson(mostGamesDay) + ","
                + "\"mostGamesForOpeningDay\": " + ServletUtils.csvToJson(mostGamesForOpeningDay) + ","
                + "\"dayWithMostUniqueOpenings\": " + ServletUtils.csvToJson(dayWithMostUniqueOpenings)
                + "}";

        ServletUtils.sendResponse(resp, jsonResponse, "application/json");
    }

    private void handleSpecificQueryRequest(HttpServletResponse resp) throws IOException {
        LOGGER.info("Richiesta per i file generati da SpecificQueryGenerator.");

        // Verifica se i file esistono
        if (dataService.fileExists("openingWinStats.csv") ||
                dataService.fileExists("ratingDistribution.csv") ||
                dataService.fileExists("timeoutOpenings.csv") ||
                dataService.fileExists("ratingDiffByOpening.csv") ||
                dataService.fileExists("drawByTimeControl.csv") ||
                dataService.fileExists("winByRatingDiff.csv")) {

            ServletUtils.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Uno o più file generati da SpecificQueryGenerator non trovato.");
            return;
        }

        // Leggi il contenuto dei file
        String openingWinStats = dataService.getFileContent("openingWinStats.csv");
        String ratingDistribution = dataService.getFileContent("ratingDistribution.csv");
        String timeoutOpenings = dataService.getFileContent("timeoutOpenings.csv");
        String ratingDiffByOpening = dataService.getFileContent("ratingDiffByOpening.csv");
        String drawByTimeControl = dataService.getFileContent("drawByTimeControl.csv");
        String winByRatingDiff = dataService.getFileContent("winByRatingDiff.csv");

        // Crea la risposta JSON
        String jsonResponse = "{"
                + "\"openingWinStats\": " + ServletUtils.csvToJson(openingWinStats) + ","
                + "\"ratingDistribution\": " + ServletUtils.csvToJson(ratingDistribution) + ","
                + "\"timeoutOpenings\": " + ServletUtils.csvToJson(timeoutOpenings) + ","
                + "\"ratingDiffByOpening\": " + ServletUtils.csvToJson(ratingDiffByOpening) + ","
                + "\"drawByTimeControl\": " + ServletUtils.csvToJson(drawByTimeControl) + ","
                + "\"winByRatingDiff\": " + ServletUtils.csvToJson(winByRatingDiff)
                + "}";

        // Invia la risposta
        ServletUtils.sendResponse(resp, jsonResponse, "application/json");
    }

    private void handleFileRequest(String filename, HttpServletResponse resp) throws IOException {
        LOGGER.info("Richiesta per il file: " + filename);
        if (dataService.fileExists(filename)) {
            ServletUtils.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "File non trovato: " + filename);
            return;
        }

        String content = dataService.getFileContent(filename);
        ServletUtils.sendResponse(resp, content, "text/csv");
    }
}
