package com.example.docling.document.domain;

public record IngestionResult(
        boolean success,
        int documentCount,
        String message
) {
    public static IngestionResult success(int documentCount) {
        return new IngestionResult(true, documentCount,
                "Successfully ingested " + documentCount + " document chunks");
    }

    public static IngestionResult failure(String message) {
        return new IngestionResult(false, 0, message);
    }
}
