package com.example.docling.document.application;

import com.example.docling.document.domain.ConversionResult;
import com.example.docling.document.domain.DocumentSource;
import ai.docling.serve.api.DoclingServeApi;
import ai.docling.serve.api.convert.request.ConvertDocumentRequest;
import ai.docling.serve.api.convert.response.ConvertDocumentResponse;
import ai.docling.serve.api.convert.request.source.FileSource;
import ai.docling.serve.api.convert.request.source.HttpSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Service
public class ConvertDocumentUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConvertDocumentUseCase.class);

    private final DoclingServeApi doclingServeApi;

    public ConvertDocumentUseCase(DoclingServeApi doclingServeApi) {
        this.doclingServeApi = doclingServeApi;
    }

    public ConversionResult execute(DocumentSource source) {
        try {
            ConvertDocumentRequest request = buildRequest(source);
            ConvertDocumentResponse response = doclingServeApi.convertSource(request);

            String markdown = response.getDocument().getMarkdownContent();
            log.info("Document converted successfully, markdown length: {}", markdown.length());

            return ConversionResult.success(markdown);
        } catch (Exception e) {
            log.error("Document conversion failed", e);
            return ConversionResult.failure(e.getMessage());
        }
    }

    private ConvertDocumentRequest buildRequest(DocumentSource source) throws Exception {
        return switch (source) {
            case DocumentSource.HttpSource httpSource -> {
                String encodedUrl = encodeUrl(httpSource.url().toString());
                yield ConvertDocumentRequest.builder()
                        .source(HttpSource.builder()
                                .url(URI.create(encodedUrl))
                                .build())
                        .build();
            }
            case DocumentSource.FileSource fileSource -> {
                byte[] fileBytes = Files.readAllBytes(Path.of(fileSource.filePath()));
                String base64Content = Base64.getEncoder().encodeToString(fileBytes);
                String filename = Path.of(fileSource.filePath()).getFileName().toString();

                yield ConvertDocumentRequest.builder()
                        .source(FileSource.builder()
                                .filename(filename)
                                .base64String(base64Content)
                                .build())
                        .build();
            }
        };
    }

    private String encodeUrl(String url) {
        if (url.contains(" ")) {
            return url.replace(" ", "%20");
        }
        return url;
    }
}
