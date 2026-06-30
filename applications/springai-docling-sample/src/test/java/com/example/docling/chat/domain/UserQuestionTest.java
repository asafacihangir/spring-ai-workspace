package com.example.docling.chat.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserQuestion")
class UserQuestionTest {

    @Test
    @DisplayName("should create UserQuestion with valid text")
    void shouldCreateUserQuestionWithValidText() {
        var questionText = "What is Docling?";
        var question = new UserQuestion(questionText);

        assertEquals(questionText, question.text());
    }

    @Test
    @DisplayName("should throw exception when text is null")
    void shouldThrowExceptionWhenTextIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new UserQuestion(null)
        );
    }

    @Test
    @DisplayName("should throw exception when text is blank")
    void shouldThrowExceptionWhenTextIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                new UserQuestion("   ")
        );
    }

    @Test
    @DisplayName("should throw exception when text is empty")
    void shouldThrowExceptionWhenTextIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new UserQuestion("")
        );
    }
}
