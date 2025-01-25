package Utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ServletUtils {



    public static String csvToJson(String csvContent) {
        String[] lines = csvContent.split("\n");
        if (lines.length < 2) {
            throw new IllegalArgumentException("Il contenuto CSV non è valido o è vuoto.");
        }

        String[] headers = lines[0].split(",");

        JsonArray jsonArray = new JsonArray();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim(); // Rimuove spazi extra all'inizio e alla fine
            if (line.isEmpty()) {
                continue; // Salta le righe vuote
            }

            String[] values = line.split(",");
            if (values.length != headers.length) {
                System.err.println("Riga malformattata ignorata: " + line);
                continue; // Salta le righe che non corrispondono al numero di intestazioni
            }

            JsonObject jsonObject = new JsonObject();
            for (int j = 0; j < headers.length; j++) {
                String cleanHeader = headers[j].trim(); // Pulisce gli spazi dall'intestazione
                String cleanValue = values[j].trim().replace("\r", ""); // Rimuove '\r' e spazi extra
                jsonObject.addProperty(cleanHeader, cleanValue);
            }
            jsonArray.add(jsonObject);
        }

        return new Gson().toJson(jsonArray);
    }



    public static void sendResponse(HttpServletResponse resp, String content, String contentType) throws IOException {
        resp.setContentType(contentType);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(content);
    }

    public static void sendError(HttpServletResponse resp, int statusCode, String errorMessage) {
        try {
            resp.setStatus(statusCode);
            resp.setContentType("text/html; charset=UTF-8");
            try (var writer = resp.getWriter()) {
                writer.write("<!DOCTYPE html>");
                writer.write("<html lang='it'>");
                writer.write("<head><meta charset='UTF-8'><title>Errore</title></head>");
                writer.write("<body style='font-family: Arial, sans-serif; text-align: center; margin-top: 50px;'>");
                writer.write("<h1 style='color: red;'>Errore " + statusCode + "</h1>");
                writer.write("<p>" + errorMessage + "</p>");
                writer.write("<hr>");
                writer.write("<p><em>Chess Analysis Server</em></p>");
                writer.write("</body>");
                writer.write("</html>");
            }
        } catch (IOException e) {
            // Gestisci eventuali errori di I/O durante l'invio della risposta
            Logger.getLogger(ServletUtils.class.getName()).log(Level.SEVERE, "Errore durante l'invio del messaggio di errore", e);
        }
    }

}


