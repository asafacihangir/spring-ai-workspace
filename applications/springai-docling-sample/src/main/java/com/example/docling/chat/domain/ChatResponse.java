package com.example.docling.chat.domain;

public record ChatResponse(String answer) {
    public static ChatResponse of(String answer) {
        return new ChatResponse(answer);
    }
}
