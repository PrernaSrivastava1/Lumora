package com.lumora.document.parser;

import com.lumora.document.DocumentParser;
import com.lumora.document.ParsedDocument;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
public class TxtParser implements DocumentParser {

    @Override
    public boolean supports(String fileType) {
        return "TXT".equalsIgnoreCase(fileType);
    }

    @Override
    public ParsedDocument parse(Path filePath) throws IOException {
        String content = Files.readString(filePath, StandardCharsets.UTF_8);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("fileSize", String.valueOf(Files.size(filePath)));
        metadata.put("parser", "TxtParser");

        return ParsedDocument.builder()
                .text(content)
                .metadata(metadata)
                .pageCount(1)
                .build();
    }
}
