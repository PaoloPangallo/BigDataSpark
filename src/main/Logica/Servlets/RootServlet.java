package Servlets;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class RootServlet extends HttpServlet {

    private String webAppDirectory;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // Recupera il parametro di inizializzazione dal web.xml
        this.webAppDirectory = config.getInitParameter("webAppDirectory");

        if (this.webAppDirectory == null || this.webAppDirectory.isEmpty()) {
            throw new ServletException("webAppDirectory non configurato o vuoto!");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Percorso del file index.html
        String filePath = webAppDirectory + File.separator + "index.html";
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
