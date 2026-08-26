package org.phoenix.coordinator.analyze;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AnalyzeService {

    private final ChatClient chatClient;

    public AnalyzeService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String analyze(String ticket) {
        return chatClient.prompt()
                .user(ticket)
                .call()
                .content();
    }
}
