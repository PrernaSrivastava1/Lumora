package com.lumora.document;

import java.io.IOException;
import java.nio.file.Path;

public interface DocumentParser {

    /**
     * Checks if this parser supports the given file extension/type.
     *
     * @param fileType the file extension (e.g., "PDF", "TXT", "DOCX", "MD")
     * @return true if supported, false otherwise
     */
    boolean supports(String fileType);

    /**
     * Parses the file at the given path and extracts its text and metadata.
     *
     * @param filePath the path to the document file
     * @return the extracted ParsedDocument details
     * @throws IOException if reading the file fails
     */
    ParsedDocument parse(Path filePath) throws IOException;
}
