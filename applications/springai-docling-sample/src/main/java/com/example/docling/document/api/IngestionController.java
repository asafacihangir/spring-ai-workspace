package com.example.docling.document.api;

import com.example.docling.document.application.IngestDocumentUseCase;
import com.example.docling.document.domain.DocumentSource;
import com.example.docling.document.domain.IngestionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/documents/ingest")
public class IngestionController {

    private final IngestDocumentUseCase ingestDocumentUseCase;

    public IngestionController(IngestDocumentUseCase ingestDocumentUseCase) {
        this.ingestDocumentUseCase = ingestDocumentUseCase;
    }

    @GetMapping("/http")
    public ResponseEntity<IngestionResult> ingestFromUrl(@RequestParam String url) {
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(IngestionResult.failure("URL is required"));
        }

        var source = new DocumentSource.HttpSource(URI.create(url));
        var result = ingestDocumentUseCase.execute(source);

        return result.success()
                ? ResponseEntity.ok(result)
                : ResponseEntity.internalServerError().body(result);
    }

    @GetMapping("/file")
    public ResponseEntity<IngestionResult> ingestFromFile(@RequestParam String path) {
        if (path == null || path.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(IngestionResult.failure("File path is required"));
        }

        var source = new DocumentSource.FileSource(path);
        var result = ingestDocumentUseCase.execute(source);

        return result.success()
                ? ResponseEntity.ok(result)
                : ResponseEntity.internalServerError().body(result);
    }

    @PostMapping
    public ResponseEntity<IngestionResult> ingest(@RequestBody IngestRequest request) {
        if (request.url() == null && request.filePath() == null) {
            return ResponseEntity.badRequest()
                    .body(IngestionResult.failure("Either url or filePath must be provided"));
        }

        DocumentSource source = request.url() != null
                ? new DocumentSource.HttpSource(URI.create(request.url()))
                : new DocumentSource.FileSource(request.filePath());

        var result = ingestDocumentUseCase.execute(source);

        return result.success()
                ? ResponseEntity.ok(result)
                : ResponseEntity.internalServerError().body(result);
    }

    public record IngestRequest(String url, String filePath) {}
}
