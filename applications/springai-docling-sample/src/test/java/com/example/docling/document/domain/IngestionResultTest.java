package com.example.docling.document.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IngestionResult")
class IngestionResultTest {

    @Test
    @DisplayName("should create success result with document count")
    void shouldCreateSuccessResult() {
        var result = IngestionResult.success(15);

        assertTrue(result.success());
        assertEquals(15, result.documentCount());
        assertEquals("Successfully ingested 15 document chunks", result.message());
    }

    @Test
    @DisplayName("should create failure result with message")
    void shouldCreateFailureResult() {
        var errorMessage = "No documents were parsed from source";
        var result = IngestionResult.failure(errorMessage);

        assertFalse(result.success());
        assertEquals(0, result.documentCount());
        assertEquals(errorMessage, result.message());
    }

    @Test
    @DisplayName("should handle zero documents in success")
    void shouldHandleZeroDocuments() {
        var result = IngestionResult.success(0);

        assertTrue(result.success());
        assertEquals(0, result.documentCount());
        assertEquals("Successfully ingested 0 document chunks", result.message());
    }
}
