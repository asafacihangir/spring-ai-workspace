package org.phoenix.todowritetool.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private final ChatClient chatClient;

    public AgentService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String run(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
