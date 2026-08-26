package org.phoenix.repoagent.config;

import java.nio.file.Path;
import java.util.List;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import org.phoenix.repoagent.tools.RepositoryTools;
import org.springaicommunity.a2a.server.executor.DefaultAgentExecutor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class A2aServerConfig {

    private static final String SYSTEM_PROMPT = """
            You are %s, a repository analysis expert for a single codebase.
            Answer questions about this repository using only the three provided tools:
            listFiles, readFile and searchInFiles.
            Orient yourself first with listFiles or searchInFiles, then read only the files you need.
            Every finding must reference a concrete file path and, where relevant, a symbol
            (class, method, configuration key).
            Never speculate about classes, files or behavior you did not find with the tools;
            if you cannot find something, say explicitly that you could not find it.
            Respond with short, well-structured Markdown.
            """;

    @Bean
    public RepositoryTools repositoryTools(RepoAgentProperties props) {
        return new RepositoryTools(Path.of(props.root()));
    }

    @Bean
    public AgentCard agentCard(RepoAgentProperties props, @Value("${server.port:8080}") int port) {
        return new AgentCard.Builder()
                .name(props.name())
                .description(props.description())
                .url("http://localhost:" + port + "/")
                .version("1.0.0")
                .capabilities(new AgentCapabilities.Builder().streaming(false).build())
                .defaultInputModes(List.of("text"))
                .defaultOutputModes(List.of("text"))
                .skills(List.of(new AgentSkill.Builder()
                        .id("repository-analysis")
                        .name("Repository analysis")
                        .description("Answers questions about the repository with file path and symbol evidence")
                        .tags(List.of("repository", "analysis"))
                        .build()))
                .protocolVersion("0.3.0")
                .build();
    }

    @Bean
    public AgentExecutor agentExecutor(ChatClient.Builder chatClientBuilder, RepoAgentProperties props,
            RepositoryTools repositoryTools) {

        ChatClient chatClient = chatClientBuilder.clone()
                .defaultSystem(SYSTEM_PROMPT.formatted(props.name()))
                .defaultTools(repositoryTools)
                .build();

        return new DefaultAgentExecutor(chatClient, (chat, requestContext) -> {
            String userMessage = DefaultAgentExecutor.extractTextFromMessage(requestContext.getMessage());
            return chat.prompt().user(userMessage).call().content();
        });
    }

}
