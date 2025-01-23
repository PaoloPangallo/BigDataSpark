package Utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;



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

    public static void sendError(HttpServletResponse resp, int statusCode, String errorMessage) throws IOException {
        resp.setStatus(statusCode);
        resp.getWriter().write("<html><body><h1>" + errorMessage + "</h1></body></html>");
    }
}
