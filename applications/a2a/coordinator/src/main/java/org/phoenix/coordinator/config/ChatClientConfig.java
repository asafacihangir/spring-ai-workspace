package org.phoenix.coordinator.config;

import java.util.ArrayList;
import java.util.List;

import org.springaicommunity.agent.advisors.AutoMemoryToolsAdvisor;
import org.springaicommunity.agent.common.task.subagent.SubagentReference;
import org.springaicommunity.agent.common.task.subagent.SubagentType;
import org.phoenix.coordinator.a2a.BlockingA2aSubagentExecutor;
import org.springaicommunity.agent.subagent.a2a.A2ASubagentDefinition;
import org.springaicommunity.agent.subagent.a2a.A2ASubagentResolver;
import org.springaicommunity.agent.tools.task.TaskTool;
import org.springaicommunity.agent.tools.task.claude.ClaudeSubagentReferences;
import org.springaicommunity.agent.tools.task.claude.ClaudeSubagentType;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    private static final String SYSTEM_PROMPT = """
            You are EngineeringCoordinatorAgent. A ticket (feature or change request) arrives as
            plain text; you produce a change impact analysis for human review.

            Through the task tool you can delegate to: remote repository agents (one expert per
            codebase), test-planner (local) and security-reviewer (local).

            Orchestration rules:
            1. Evidence first: never speculate about code. Ask the relevant repository agent(s)
               for evidence (file paths, classes, current behavior) before drawing conclusions.
            2. Call only the repository agent(s) whose codebase is actually affected by the ticket.
            3. Call security-reviewer ONLY when identity, authorization, personal data or external
               input is affected by the change.
            4. Call test-planner in every analysis, but only AFTER repository evidence has been
               collected; include that evidence in its prompt.
            5. If a subagent cannot be reached, write "<ajan adı> ajanına ulaşılamadı" in the
               report and do not invent findings on its behalf.
            6. If findings conflict, do not hide the conflict; surface it under "Açık kararlar".
            7. Every technical claim in the report must cite a file path or symbol from the
               collected evidence.
            8. Memory is for durable architectural rules only (e.g. backward compatibility
               policies); never store volatile facts such as build status or ticket lists.

            The final report MUST be written in Turkish, in Markdown, with exactly these sections:

            ## İstek
            ## Etkilenen bileşenler
            ## Önerilen akış
            ## Riskler
            ## Test planı
            ## Açık kararlar

            You only produce an analysis for human review. Never modify code and never claim that
            any change was implemented.
            """;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, CoordinatorProperties props) {
        ChatClient.Builder subagentBuilder = builder.clone();

        ToolCallback taskTool = TaskTool.builder()
                .subagentTypes(
                        ClaudeSubagentType.builder().chatClientBuilder("default", subagentBuilder).build(),
                        new SubagentType(new A2ASubagentResolver(), new BlockingA2aSubagentExecutor()))
                .subagentReferences(subagentReferences(props))
                .build();

        return builder.defaultSystem(SYSTEM_PROMPT)
                .defaultTools(taskTool)
                .defaultAdvisors(AutoMemoryToolsAdvisor.builder()
                        .memoriesRootDirectory(props.memoryDir())
                        .build())
                .build();
    }

    private List<SubagentReference> subagentReferences(CoordinatorProperties props) {
        List<SubagentReference> references = new ArrayList<>(ClaudeSubagentReferences.fromRootDirectory(props.agentsDir()));
        for (String url : props.repoAgentUrls()) {
            references.add(new SubagentReference(url, A2ASubagentDefinition.KIND));
        }
        return references;
    }

}
