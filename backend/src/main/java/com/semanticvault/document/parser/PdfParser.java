package com.semanticvault.document.parser;

import com.semanticvault.document.DocumentParser;
import com.semanticvault.document.ParsedDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
public class PdfParser implements DocumentParser {

    @Override
    public boolean supports(String fileType) {
        return "PDF".equalsIgnoreCase(fileType);
    }

    @Override
    public ParsedDocument parse(Path filePath) throws IOException {
        try (PDDocument doc = Loader.loadPDF(filePath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            int pageCount = doc.getNumberOfPages();

            Map<String, String> metadata = new HashMap<>();
            metadata.put("parser", "PdfParser");
            metadata.put("pageCount", String.valueOf(pageCount));
            
            if (doc.getDocumentInformation() != null) {
                String title = doc.getDocumentInformation().getTitle();
                if (title != null && !title.isBlank()) {
                    metadata.put("pdfTitle", title);
                }
                String author = doc.getDocumentInformation().getAuthor();
                if (author != null && !author.isBlank()) {
                    metadata.put("pdfAuthor", author);
                }
            }

            return ParsedDocument.builder()
                    .text(text)
                    .metadata(metadata)
                    .pageCount(pageCount)
                    .build();
        }
    }
}
