package com.example.docling.document.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConversionResult")
class ConversionResultTest {

    @Test
    @DisplayName("should create success result with markdown content")
    void shouldCreateSuccessResult() {
        var markdown = "# Document Title\n\nContent here...";
        var result = ConversionResult.success(markdown);

        assertTrue(result.success());
        assertEquals(markdown, result.markdownContent());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    @DisplayName("should create failure result with single error")
    void shouldCreateFailureResultWithSingleError() {
        var errorMessage = "Connection timeout";
        var result = ConversionResult.failure(errorMessage);

        assertFalse(result.success());
        assertNull(result.markdownContent());
        assertEquals(1, result.errors().size());
        assertEquals(errorMessage, result.errors().get(0));
    }

    @Test
    @DisplayName("should create failure result with multiple errors")
    void shouldCreateFailureResultWithMultipleErrors() {
        var errors = List.of("Error 1", "Error 2", "Error 3");
        var result = ConversionResult.failure(errors);

        assertFalse(result.success());
        assertNull(result.markdownContent());
        assertEquals(3, result.errors().size());
        assertEquals(errors, result.errors());
    }
}
