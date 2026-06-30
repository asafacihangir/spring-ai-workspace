package com.example.docling.document.application;

import com.example.docling.document.domain.DocumentSource;
import com.example.docling.document.domain.IngestionResult;
import io.arconia.ai.document.docling.DoclingDocumentReader;
import io.arconia.docling.client.DoclingServeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Service
public class IngestDocumentUseCase {

    private static final Logger log = LoggerFactory.getLogger(IngestDocumentUseCase.class);

    private final DoclingServeClient doclingServeClient;
    private final VectorStore vectorStore;

    public IngestDocumentUseCase(DoclingServeClient doclingServeClient, VectorStore vectorStore) {
        this.doclingServeClient = doclingServeClient;
        this.vectorStore = vectorStore;
    }

    public IngestionResult execute(DocumentSource source) {
        try {
            Resource resource = toResource(source);

            List<Document> documents = DoclingDocumentReader.builder()
                    .doclingServeApi(doclingServeClient)
                    .files(resource)
                    .build()
                    .get();

            if (documents.isEmpty()) {
                log.warn("No documents were parsed from source");
                return IngestionResult.failure("No documents were parsed from source");
            }

            vectorStore.add(documents);
            log.info("Successfully ingested {} document chunks", documents.size());

            return IngestionResult.success(documents.size());
        } catch (Exception e) {
            log.error("Document ingestion failed", e);
            return IngestionResult.failure(e.getMessage());
        }
    }

    private Resource toResource(DocumentSource source) throws Exception {
        return switch (source) {
            case DocumentSource.HttpSource httpSource -> {
                String urlString = httpSource.url().toString();
                if (urlString.contains(" ")) {
                    urlString = urlString.replace(" ", "%20");
                }
                yield new UrlResource(URI.create(urlString));
            }
            case DocumentSource.FileSource fileSource ->
                    new FileSystemResource(fileSource.filePath());
        };
    }
}
