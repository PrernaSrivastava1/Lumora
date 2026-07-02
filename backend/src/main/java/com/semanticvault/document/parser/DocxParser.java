package com.semanticvault.document.parser;

import com.semanticvault.document.DocumentParser;
import com.semanticvault.document.ParsedDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
public class DocxParser implements DocumentParser {

    @Override
    public boolean supports(String fileType) {
        return "DOCX".equalsIgnoreCase(fileType);
    }

    @Override
    public ParsedDocument parse(Path filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {

            String text = extractor.getText();
            Map<String, String> metadata = new HashMap<>();
            metadata.put("parser", "DocxParser");

            return ParsedDocument.builder()
                    .text(text)
                    .metadata(metadata)
                    .pageCount(1)
                    .build();
        }
    }
}
