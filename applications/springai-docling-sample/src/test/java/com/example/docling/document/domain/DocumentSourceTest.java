package com.example.docling.document.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DocumentSource")
class DocumentSourceTest {

    @Nested
    @DisplayName("HttpSource")
    class HttpSourceTest {

        @Test
        @DisplayName("should create HttpSource with valid URL")
        void shouldCreateHttpSourceWithValidUrl() {
            var url = URI.create("https://example.com/document.pdf");
            var source = new DocumentSource.HttpSource(url);

            assertEquals(url, source.url());
        }

        @Test
        @DisplayName("should throw exception when URL is null")
        void shouldThrowExceptionWhenUrlIsNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    new DocumentSource.HttpSource(null)
            );
        }
    }

    @Nested
    @DisplayName("FileSource")
    class FileSourceTest {

        @Test
        @DisplayName("should create FileSource with valid path")
        void shouldCreateFileSourceWithValidPath() {
            var path = "/path/to/document.pdf";
            var source = new DocumentSource.FileSource(path);

            assertEquals(path, source.filePath());
        }

        @Test
        @DisplayName("should throw exception when path is null")
        void shouldThrowExceptionWhenPathIsNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    new DocumentSource.FileSource(null)
            );
        }

        @Test
        @DisplayName("should throw exception when path is blank")
        void shouldThrowExceptionWhenPathIsBlank() {
            assertThrows(IllegalArgumentException.class, () ->
                    new DocumentSource.FileSource("   ")
            );
        }
    }

    @Test
    @DisplayName("should be sealed interface with HttpSource and FileSource")
    void shouldBeSealedInterface() {
        var httpSource = new DocumentSource.HttpSource(URI.create("https://example.com/doc.pdf"));
        var fileSource = new DocumentSource.FileSource("/path/to/doc.pdf");

        assertInstanceOf(DocumentSource.class, httpSource);
        assertInstanceOf(DocumentSource.class, fileSource);
    }
}
