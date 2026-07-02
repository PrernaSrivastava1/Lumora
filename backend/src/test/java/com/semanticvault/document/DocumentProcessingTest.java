package com.semanticvault.document;

import com.semanticvault.document.parser.DocxParser;
import com.semanticvault.document.parser.MarkdownParser;
import com.semanticvault.document.parser.PdfParser;
import com.semanticvault.document.parser.TxtParser;
import com.semanticvault.service.ProcessingService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DocumentProcessingTest {

    @Autowired
    private ProcessingService processingService;

    @Autowired
    private TxtParser txtParser;

    @Autowired
    private PdfParser pdfParser;

    @Autowired
    private DocxParser docxParser;

    @Autowired
    private MarkdownParser markdownParser;

    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        tempDir = Files.createTempDirectory("parser-tests");
    }

    @Test
    void testTxtParser() throws Exception {
        Path txtFile = tempDir.resolve("sample.txt");
        Files.writeString(txtFile, "Hello plain text parsing!");

        ParsedDocument doc = txtParser.parse(txtFile);
        assertEquals("Hello plain text parsing!", doc.getText());
        assertEquals(1, doc.getPageCount());
        assertEquals("TxtParser", doc.getMetadata().get("parser"));
    }

    @Test
    void testMarkdownParser() throws Exception {
        Path mdFile = tempDir.resolve("sample.md");
        String mdContent = """
                ---
                title: MD Doc
                ---
                # Header Title
                This is **bold** text and [Google](https://google.com) link.
                """;
        Files.writeString(mdFile, mdContent);

        ParsedDocument doc = markdownParser.parse(mdFile);
        String cleaned = doc.getText();
        assertTrue(cleaned.contains("Header Title"));
        assertTrue(cleaned.contains("bold"));
        assertTrue(cleaned.contains("Google"));
        // Check formatting characters are stripped
        assertTrue(!cleaned.contains("**"));
        assertTrue(!cleaned.contains("[Google]"));
        assertEquals("MarkdownParser", doc.getMetadata().get("parser"));
    }

    @Test
    void testPdfParser() throws Exception {
        Path pdfFile = tempDir.resolve("sample.pdf");
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream contents = new PDPageContentStream(doc, page)) {
                contents.beginText();
                // PDFBox 3.x constructor using Standard14Fonts
                PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                contents.setFont(font, 12);
                contents.newLineAtOffset(100, 700);
                contents.showText("Hello from PDFBox!");
                contents.endText();
            }
            doc.save(pdfFile.toFile());
        }

        ParsedDocument doc = pdfParser.parse(pdfFile);
        assertTrue(doc.getText().contains("Hello from PDFBox!"));
        assertEquals(1, doc.getPageCount());
        assertEquals("PdfParser", doc.getMetadata().get("parser"));
    }

    @Test
    void testDocxParser() throws Exception {
        Path docxFile = tempDir.resolve("sample.docx");
        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(docxFile.toFile())) {
            var paragraph = document.createParagraph();
            var run = paragraph.createRun();
            run.setText("Hello word file POI extraction!");
            document.write(out);
        }

        ParsedDocument doc = docxParser.parse(docxFile);
        assertTrue(doc.getText().contains("Hello word file POI extraction!"));
        assertEquals(1, doc.getPageCount());
        assertEquals("DocxParser", doc.getMetadata().get("parser"));
    }

    @Test
    void testProcessingServiceOrchestration() throws Exception {
        Path txtFile = tempDir.resolve("orchestrated.txt");
        Files.writeString(txtFile, "Orchestrated text pipeline.");

        ParsedDocument doc = processingService.processDocument(txtFile, "TXT");
        assertEquals("Orchestrated text pipeline.", doc.getText());

        // Verify clean text file exists
        Path cleanFile = java.nio.file.Paths.get("uploads", "clean", "orchestrated.txt.clean.txt").toAbsolutePath().normalize();
        try {
            assertTrue(Files.exists(cleanFile));
            assertEquals("Orchestrated text pipeline.", Files.readString(cleanFile));
        } finally {
            Files.deleteIfExists(cleanFile);
        }
    }
}
