package com.example.docling.chat.domain;

public record UserQuestion(String text) {
    public UserQuestion {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Question text cannot be null or blank");
        }
    }
}
