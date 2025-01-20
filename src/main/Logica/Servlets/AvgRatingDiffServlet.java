package Servlets;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

public class AvgRatingDiffServlet extends HttpServlet {

    private String outputDirectory;

    // Costruttore con parametro
    public AvgRatingDiffServlet(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String filePath = "C:\\Users\\paolo\\Desktop\\ProgettoSparkScacchi\\Output\\avgRatingDiff.csv";
        File file = new File(filePath);

        if (!file.exists()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("File non trovato");
            return;
        }

        // Impostiamo il tipo di contenuto come CSV
        resp.setContentType("text/csv");
        resp.setStatus(HttpServletResponse.SC_OK);

        // Leggi il contenuto del file CSV e invialo nella risposta
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                resp.getWriter().write(line + "\n");
            }
        }
    }
}
