package Utils;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ServletUtils {
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
