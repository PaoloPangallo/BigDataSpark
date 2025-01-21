package Servlets;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class AvgRatingDiffServlet extends HttpServlet {

    private String outputDirectory;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // Recupera il parametro di inizializzazione dal web.xml
        this.outputDirectory = config.getInitParameter("outputDirectory");

        if (this.outputDirectory == null || this.outputDirectory.isEmpty()) {
            throw new ServletException("outputDirectory non configurato o vuoto!");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Costruisce il percorso del file usando outputDirectory
        String filePath = outputDirectory + File.separator + "avgRatingDiff.csv";
        File file = new File(filePath);

        // Se il file non esiste, restituisce un errore 404
        if (!file.exists()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("File non trovato");
            return;
        }

        // Imposta il tipo di contenuto come CSV
        resp.setContentType("text/csv");
        resp.setStatus(HttpServletResponse.SC_OK);

        // Legge il contenuto del file CSV e lo invia nella risposta
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                resp.getWriter().write(line + "\n");
            }
        }
    }
}
