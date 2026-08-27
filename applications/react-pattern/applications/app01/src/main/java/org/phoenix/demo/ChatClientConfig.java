package org.phoenix.demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class ChatClientConfig {

  @Bean
  ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
    return chatClientBuilder.build();
  }

  @Bean
  ChatClientBuilderCustomizer addTools(DisneyParksTools disneyParksTools) {
    return builder -> {
      builder.defaultTools(disneyParksTools);
    };
  }

  @Bean
  ChatClientBuilderCustomizer addLoggingAdvisor() {
    return builder -> {
      builder.defaultAdvisors(SimpleLoggerAdvisor.builder()
          .requestToString(ChatClientConfig::formatRequest)
          .responseToString(ChatClientConfig::formatResponse)
          .build());
    };
  }

  // ponytail: only the newest message is printed - the older ones were already logged on earlier turns
  private static String formatRequest(ChatClientRequest request) {
    List<Message> messages = request.prompt().getInstructions();
    Message last = messages.getLast();
    String body = switch (last) {
      case ToolResponseMessage m -> m.getResponses().stream()
          .map(r -> "    OBSERVE %s -> %s".formatted(r.name(), r.responseData()))
          .collect(Collectors.joining("\n"));
      default -> "    " + last.getText().strip();
    };
    return "\n>>> SENDING %d message(s), newest is %s:\n%s".formatted(messages.size(), last.getMessageType(), body);
  }

  private static String formatResponse(ChatResponse response) {
    var output = response.getResult().getOutput();
    String body = output.getToolCalls().isEmpty()
        ? "    " + output.getText().strip()
        : output.getToolCalls().stream()
            .map(call -> "    ACT %s(%s)".formatted(call.name(), call.arguments()))
            .collect(Collectors.joining("\n"));
    return "<<< GOT %s, %d tokens total:\n%s\n".formatted(
        response.getResult().getMetadata().getFinishReason(),
        response.getMetadata().getUsage().getTotalTokens(),
        body);
  }

}
