package com.lumora.service;

import com.lumora.document.ParsedDocument;
import org.springframework.stereotype.Service;
import java.nio.file.Path;

@Service
public class TextExtractionService {

    private final ProcessingService processingService;

    public TextExtractionService(ProcessingService processingService) {
        this.processingService = processingService;
    }

    public String extractText(Path filePath, String extension) throws Exception {
        ParsedDocument parsed = processingService.processDocument(filePath, extension.toUpperCase());
        return parsed.getText();
    }
}
