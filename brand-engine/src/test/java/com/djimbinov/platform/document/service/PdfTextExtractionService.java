package com.djimbinov.platform.document.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class PdfTextExtractionService {

  public String extractText(Path pdfPath) {

    try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {

      PDFTextStripper stripper = new PDFTextStripper();

      return stripper.getText(document);

    } catch (IOException exception) {

      throw new IllegalStateException(
            "Failed to extract text from PDF",
            exception
      );
    }
  }
}