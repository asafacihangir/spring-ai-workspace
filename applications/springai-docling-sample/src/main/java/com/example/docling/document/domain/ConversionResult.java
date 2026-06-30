package com.example.docling.document.domain;

import java.util.List;

public record ConversionResult(
        boolean success,
        String markdownContent,
        List<String> errors
) {
    public static ConversionResult success(String markdownContent) {
        return new ConversionResult(true, markdownContent, List.of());
    }

    public static ConversionResult failure(String error) {
        return new ConversionResult(false, null, List.of(error));
    }

    public static ConversionResult failure(List<String> errors) {
        return new ConversionResult(false, null, errors);
    }
}
