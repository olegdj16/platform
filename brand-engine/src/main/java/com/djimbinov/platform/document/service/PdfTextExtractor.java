package com.djimbinov.platform.document.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class PdfTextExtractor {

  public String extract(Path path) {
    if (path == null) {
      throw new IllegalArgumentException(
            "PDF path must not be null"
      );
    }

    try (PDDocument document =
               Loader.loadPDF(path.toFile())) {

      PDFTextStripper stripper =
            new PDFTextStripper();

      return stripper.getText(document).trim();

    } catch (IOException exception) {
      throw new IllegalStateException(
            "Failed to extract text from PDF: " + path,
            exception
      );
    }
  }
}