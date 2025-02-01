package Services;

import java.io.*;
import java.nio.file.Files;

public class DataService {
    private final String outputDirectory;

    public DataService(String outputDirectory) {
        if (outputDirectory == null || outputDirectory.isEmpty()) {
            throw new IllegalArgumentException("Output directory non valida.");
        }
        this.outputDirectory = outputDirectory;
    }

    public String getFileContent(String fileName) throws IOException {
        File file = new File(outputDirectory, fileName);
        if (!file.exists()) {
            throw new IOException("File non trovato: " + file.getAbsolutePath());
        }
        return new String(Files.readAllBytes(file.toPath()));

    }
    public boolean fileExists(String filename) {
        File file = new File(outputDirectory, filename);
        return !file.exists();
    }
}
