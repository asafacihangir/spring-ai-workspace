package com.example.docling.document.api;

import com.example.docling.document.application.ConvertDocumentUseCase;
import com.example.docling.document.domain.ConversionResult;
import com.example.docling.document.domain.DocumentSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/documents/convert")
public class ConversionController {

    private final ConvertDocumentUseCase convertDocumentUseCase;

    public ConversionController(ConvertDocumentUseCase convertDocumentUseCase) {
        this.convertDocumentUseCase = convertDocumentUseCase;
    }

    @GetMapping("/http")
    public ResponseEntity<ConversionResult> convertFromUrl(@RequestParam String url) {
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ConversionResult.failure("URL is required"));
        }

        var source = new DocumentSource.HttpSource(URI.create(url));
        var result = convertDocumentUseCase.execute(source);

        return result.success()
                ? ResponseEntity.ok(result)
                : ResponseEntity.internalServerError().body(result);
    }

    @PostMapping
    public ResponseEntity<ConversionResult> convert(@RequestBody ConvertRequest request) {
        if (request.url() == null && request.filePath() == null) {
            return ResponseEntity.badRequest()
                    .body(ConversionResult.failure("Either url or filePath must be provided"));
        }

        DocumentSource source = request.url() != null
                ? new DocumentSource.HttpSource(URI.create(request.url()))
                : new DocumentSource.FileSource(request.filePath());

        var result = convertDocumentUseCase.execute(source);

        return result.success()
                ? ResponseEntity.ok(result)
                : ResponseEntity.internalServerError().body(result);
    }

    public record ConvertRequest(String url, String filePath) {}
}
