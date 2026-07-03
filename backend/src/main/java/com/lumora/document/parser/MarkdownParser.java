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
import java.util.regex.Pattern;

@Component
public class MarkdownParser implements DocumentParser {

    private static final Pattern HEADERS = Pattern.compile("^#{1,6}\\s+", Pattern.MULTILINE);
    private static final Pattern BOLD_ITALIC = Pattern.compile("[*_]{1,3}");
    private static final Pattern LINKS = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");
    private static final Pattern INLINE_CODE = Pattern.compile("`([^`]+)`");
    private static final Pattern FRONTMATTER = Pattern.compile("^---$\\r?\\n(.*?)\\r?\\n^---$", Pattern.DOTALL | Pattern.MULTILINE);

    @Override
    public boolean supports(String fileType) {
        return "MD".equalsIgnoreCase(fileType) || "MARKDOWN".equalsIgnoreCase(fileType);
    }

    @Override
    public ParsedDocument parse(Path filePath) throws IOException {
        String content = Files.readString(filePath, StandardCharsets.UTF_8);

        // Strip frontmatter if present
        String cleanText = FRONTMATTER.matcher(content).replaceAll("");

        // Strip headers
        cleanText = HEADERS.matcher(cleanText).replaceAll("");

        // Strip link brackets keeping link text
        cleanText = LINKS.matcher(cleanText).replaceAll("$1");

        // Strip inline code backticks
        cleanText = INLINE_CODE.matcher(cleanText).replaceAll("$1");

        // Strip bold/italic markup symbols
        cleanText = BOLD_ITALIC.matcher(cleanText).replaceAll("");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("parser", "MarkdownParser");

        return ParsedDocument.builder()
                .text(cleanText.trim())
                .metadata(metadata)
                .pageCount(1)
                .build();
    }
}
