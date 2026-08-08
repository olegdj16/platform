package com.djimbinov.platform.document.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PdfTextExtractorTest {

  private final PdfTextExtractor pdfTextExtractor =
        new PdfTextExtractor();

  @TempDir
  Path tempDir;

  @Test
  void extract_shouldExtractTextFromPdf()
        throws IOException {

    Path pdfPath =
          tempDir.resolve("test.pdf");

    try (PDDocument document =
               new PDDocument()) {

      PDPage page = new PDPage();
      document.addPage(page);

      try (PDPageContentStream contentStream =
                 new PDPageContentStream(
                       document,
                       page
                 )) {

        contentStream.beginText();

        contentStream.setFont(
              new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA
              ),
              12
        );

        contentStream.newLineAtOffset(
              100,
              700
        );

        contentStream.showText(
              "Brand Engine PDF extraction test."
        );

        contentStream.endText();
      }

      document.save(pdfPath.toFile());
    }

    String result =
          pdfTextExtractor.extract(pdfPath);

    assertEquals(
          "Brand Engine PDF extraction test.",
          result
    );
  }

  @Test
  void extract_shouldRejectNullPath() {
    assertThrows(
          IllegalArgumentException.class,
          () -> pdfTextExtractor.extract(null)
    );
  }

  @Test
  void extract_shouldThrowWhenFileDoesNotExist() {
    Path missing =
          tempDir.resolve("missing.pdf");

    assertThrows(
          IllegalStateException.class,
          () -> pdfTextExtractor.extract(missing)
    );
  }
}