package org.phoenix.coordinator.a2a;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.agent.common.task.subagent.SubagentDefinition;
import org.springaicommunity.agent.common.task.subagent.SubagentExecutor;
import org.springaicommunity.agent.common.task.subagent.TaskCall;
import org.springaicommunity.agent.subagent.a2a.A2ASubagentDefinition;

/**
 * A2A subagent executor that sends a blocking JSON-RPC {@code message/send} and returns
 * the completed task's artifact text.
 *
 * Replaces the stock {@code A2ASubagentExecutor} (agent-utils 0.10.0), which returns after
 * the first task event: spring-ai-a2a-server 0.3.0 answers a non-blocking send with a
 * WORKING snapshot and then closes the event queue, so the stock executor yields an empty
 * response and the task result is never even written to the server's task store (polling
 * is therefore not an option either). With {@code configuration.blocking=true} the server
 * holds the response until the task completes and includes the artifacts.
 */
public class BlockingA2aSubagentExecutor implements SubagentExecutor {

    private static final Logger logger = LoggerFactory.getLogger(BlockingA2aSubagentExecutor.class);

    private static final Duration RESPONSE_TIMEOUT = Duration.ofMinutes(6);

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getKind() {
        return A2ASubagentDefinition.KIND;
    }

    @Override
    public String execute(TaskCall taskCall, SubagentDefinition definition) {
        String agentUrl = ((A2ASubagentDefinition) definition).getAgentCard().url();
        try {
            logger.info("Delegating to A2A agent '{}' at {}", definition.getName(), agentUrl);
            String response = sendBlocking(agentUrl, taskCall.prompt());
            logger.info("A2A agent '{}' returned {} characters", definition.getName(), response.length());
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error communicating with agent '%s': interrupted".formatted(definition.getName());
        } catch (Exception e) {
            logger.error("A2A agent '{}' call failed", definition.getName(), e);
            return "Error communicating with agent '%s': %s".formatted(definition.getName(), e.getMessage());
        }
    }

    private String sendBlocking(String agentUrl, String prompt) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(agentUrl))
                .timeout(RESPONSE_TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(messageSendRequest(prompt))))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("A2A message/send failed with HTTP " + response.statusCode());
        }
        return extractResult(objectMapper.readTree(response.body()));
    }

    private ObjectNode messageSendRequest(String prompt) {
        ObjectNode message = objectMapper.createObjectNode()
                .put("kind", "message")
                .put("messageId", UUID.randomUUID().toString())
                .put("role", "user");
        message.putArray("parts").addObject().put("kind", "text").put("text", prompt);

        ObjectNode params = objectMapper.createObjectNode();
        params.set("message", message);
        ObjectNode configuration = params.putObject("configuration");
        configuration.put("blocking", true);
        configuration.putArray("acceptedOutputModes").add("text");

        ObjectNode request = objectMapper.createObjectNode()
                .put("jsonrpc", "2.0")
                .put("id", UUID.randomUUID().toString())
                .put("method", "message/send");
        request.set("params", params);
        return request;
    }

    String extractResult(JsonNode responseBody) {
        JsonNode error = responseBody.path("error");
        if (error.isObject()) {
            throw new IllegalStateException("A2A error response: " + error.path("message").asText());
        }
        JsonNode result = responseBody.path("result");
        if ("message".equals(result.path("kind").asText())) {
            return textOf(result.path("parts"));
        }
        String state = result.path("status").path("state").asText();
        if (!"completed".equals(state)) {
            return "Agent task ended in state '" + state + "' without a result";
        }
        StringBuilder text = new StringBuilder();
        for (JsonNode artifact : result.path("artifacts")) {
            text.append(textOf(artifact.path("parts")));
        }
        return text.toString();
    }

    private String textOf(JsonNode parts) {
        StringBuilder text = new StringBuilder();
        for (JsonNode part : parts) {
            if ("text".equals(part.path("kind").asText())) {
                text.append(part.path("text").asText());
            }
        }
        return text.toString();
    }

}
