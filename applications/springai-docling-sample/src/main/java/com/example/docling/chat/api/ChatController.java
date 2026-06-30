package com.example.docling.chat.api;

import com.example.docling.chat.application.ProcessQueryUseCase;
import com.example.docling.chat.domain.ChatResponse;
import com.example.docling.chat.domain.UserQuestion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ProcessQueryUseCase processQueryUseCase;

    public ChatController(ProcessQueryUseCase processQueryUseCase) {
        this.processQueryUseCase = processQueryUseCase;
    }

    @GetMapping
    public ResponseEntity<ChatResponse> chat(@RequestParam String question) {
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        var userQuestion = new UserQuestion(question);
        var response = processQueryUseCase.execute(userQuestion);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chatPost(@RequestBody ChatRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        var userQuestion = new UserQuestion(request.question());
        var response = processQueryUseCase.execute(userQuestion);

        return ResponseEntity.ok(response);
    }

    public record ChatRequest(String question) {}
}
