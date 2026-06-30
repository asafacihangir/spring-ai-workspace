package com.example.docling.chat.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChatResponse")
class ChatResponseTest {

    @Test
    @DisplayName("should create ChatResponse with answer")
    void shouldCreateChatResponseWithAnswer() {
        var answer = "Docling is an open-source document parsing library.";
        var response = new ChatResponse(answer);

        assertEquals(answer, response.answer());
    }

    @Test
    @DisplayName("should create ChatResponse using static factory method")
    void shouldCreateChatResponseUsingFactoryMethod() {
        var answer = "This is the answer.";
        var response = ChatResponse.of(answer);

        assertEquals(answer, response.answer());
    }

    @Test
    @DisplayName("should handle null answer")
    void shouldHandleNullAnswer() {
        var response = ChatResponse.of(null);

        assertNull(response.answer());
    }

    @Test
    @DisplayName("should handle empty answer")
    void shouldHandleEmptyAnswer() {
        var response = ChatResponse.of("");

        assertEquals("", response.answer());
    }
}
