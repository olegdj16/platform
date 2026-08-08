package com.djimbinov.platform.document.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PdfTextExtractionServiceTest {

  @Test
  void extractTextShouldReadPdf() {

    PdfTextExtractionService service =
          new PdfTextExtractionService();

    String text = service.extractText(
          Path.of("architecture.pdf")
    );

    assertNotNull(text);
    assertFalse(text.isBlank());

    assertTrue(
          text.contains("Brand Engine")
    );
  }
}