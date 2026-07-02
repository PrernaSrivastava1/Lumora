package com.semanticvault.service;

import com.semanticvault.document.DocumentParser;
import com.semanticvault.document.ParsedDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ProcessingService {

    private final List<DocumentParser> parsers;
    private final Path cleanPath;

    public ProcessingService(List<DocumentParser> parsers) {
        this.parsers = parsers;
        this.cleanPath = Paths.get("uploads", "clean").toAbsolutePath().normalize();
        try {
            Files.createDirectories(cleanPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize clean text directory on disk", e);
        }
    }

    /**
     * Finds the matching parser, extracts plain text, and stores the clean text file.
     */
    public ParsedDocument processDocument(Path filePath, String fileType) throws IOException {
        DocumentParser parser = parsers.stream()
                .filter(p -> p.supports(fileType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No parser found supporting file type: " + fileType));

        ParsedDocument parsed = parser.parse(filePath);

        // Store clean text locally
        String cleanFileName = filePath.getFileName().toString() + ".clean.txt";
        Path targetLocation = this.cleanPath.resolve(cleanFileName);
        Files.writeString(targetLocation, parsed.getText(), StandardCharsets.UTF_8);

        return parsed;
    }
}
