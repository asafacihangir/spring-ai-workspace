package com.example.docling.document.domain;

import java.net.URI;

public sealed interface DocumentSource permits DocumentSource.HttpSource, DocumentSource.FileSource {

    record HttpSource(URI url) implements DocumentSource {
        public HttpSource {
            if (url == null) {
                throw new IllegalArgumentException("URL cannot be null");
            }
        }
    }

    record FileSource(String filePath) implements DocumentSource {
        public FileSource {
            if (filePath == null || filePath.isBlank()) {
                throw new IllegalArgumentException("File path cannot be null or blank");
            }
        }
    }
}
