package Servlets;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class RootServlet extends HttpServlet {

    // Modifica il percorso per puntare correttamente alla directory dei file statici
    private static final String WEB_APP_DIRECTORY = "src/main/webapp"; // Assicurati che questo sia il percorso corretto

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Percorso del file index.html
        String filePath = WEB_APP_DIRECTORY + "/index.html";
        File file = new File(filePath);

        // Verifica che il file esista
        if (!file.exists()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("<html><body><h1>File non trovato!</h1></body></html>");
            return;
        }

        // Imposta il tipo di contenuto come HTML
        resp.setContentType("text/html");
        resp.setStatus(HttpServletResponse.SC_OK);

        // Leggi il contenuto del file e invialo nella risposta
        byte[] content = Files.readAllBytes(file.toPath());
        resp.getOutputStream().write(content);
    }
}
