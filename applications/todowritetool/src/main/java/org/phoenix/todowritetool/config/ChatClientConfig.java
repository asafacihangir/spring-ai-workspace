package org.phoenix.todowritetool.config;

import org.phoenix.todowritetool.agent.TodoProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.agent.tools.TodoWriteTool;
import org.springaicommunity.agent.tools.TodoWriteTool.Todos;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(ChatClientConfig.class);

    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }

    @Bean
    TodoWriteTool todoWriteTool() {
        return TodoWriteTool.builder()
                .todoEventHandler(this::logProgress)
                .build();
    }

    @Bean
    ChatClientBuilderCustomizer addAgentTools(TodoWriteTool todoWriteTool) {
        return builder -> builder
                .defaultSystem("""
                        You are an agent that handles multi-step tasks. For any task with 3 or more
                        steps, you MUST FIRST call the `TodoWrite` tool to lay out your plan. Then
                        execute the steps one at a time: mark exactly ONE todo as `in_progress` before
                        starting it, and mark it `completed` immediately after finishing. Keep the todo
                        list updated in real time via `TodoWrite`. Before producing your final answer,
                        make a final `TodoWrite` call marking every todo as `completed`. Respond with
                        ONLY the final answer (no todo metadata, no commentary about the tool).
                        """)
                .defaultTools(todoWriteTool);
    }

    private void logProgress(Todos todos) {
        logger.info("TodoWrite ilerleme:\n{}", TodoProgress.format(todos));
    }
}
