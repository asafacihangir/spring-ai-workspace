package com.ai.demo.node;

import com.ai.demo.tool.model.TavilyResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
@Slf4j
public class WebSearchNode implements NodeAction {

    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState state) {
        String query = state.value("question", "");
        TavilyResponse response = chatClient.prompt()
                .system(s -> s.param("date", LocalDate.now().toString())).user(u -> u.param("question", query)).call()
                .entity(TavilyResponse.class);


        if (response == null || response.getResults() == null) {
            log.warn("WebSearchNode received null response for query: {}", query);
            return Map.of("question", query, "documents", List.of());
        }
        log.debug("WebSearchNode response: {}", response);

        // Get content and convert to Document objects
        List<Document> documents = response.getResults().stream()
                .map(result -> new Document(result.getContent(),
                        Map.of("origin", result.getUrl(),
                                "title", result.getTitle())))
                .collect(Collectors.toList());
        if (response.getAnswer() != null && !response.getAnswer().isBlank()) {
            documents.addFirst(new Document(response.getAnswer(),
                    Map.of("origin", "Web Search Answer", "title", "Web Search Answer")));
        }

        // Update state
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("question", query);
        resultMap.put("documents", documents);
        return resultMap;
    }
}
