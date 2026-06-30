package com.ai.demo.tool;

import com.ai.demo.tool.model.TavilyRequest;
import com.ai.demo.tool.model.TavilyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;

@Component
@Slf4j
public class WebSearchTool {

    private final WebClient webClient;

    public WebSearchTool(WebClient.Builder webClientBuilder,
                         @Value("${tavily.base-url}") String baseUrl,
                         @Value("${tavily.api-key}") String apiKey) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    /**
     * Performs a web search using the Tavily API.
     *
     * @param query search query containing the query parameters and other optional parameters.
     * @return TavilyResponse containing search results and related information.
     */
    @Tool(description = "Perform a web search using the Tavily API")
    public TavilyResponse search(@ToolParam(description = "search query to look up") String query) {

        TavilyRequest request = TavilyRequest.builder()
                .query(query)
                .maxResults(3) // Default: return 3 results
                .searchDepth("basic") // Default search depth: basic
                .includeAnswer(true) // Include synthesized answer from search results
                .includeRawContent(false) // Default: do not include raw content
                .includeImages(false) // Default: do not include images
                .build();

        if (request.getQuery() == null || request.getQuery().isEmpty()) {
            throw new IllegalArgumentException("Query parameter is required.");
        }
        log.info("Received TavilyRequest: {}", request);

        // Build the request payload with all parameters, setting defaults where necessary
        TavilyRequest requestWithApiKey = TavilyRequest.builder().query(request.getQuery())
                .searchDepth(request.getSearchDepth() != null ? request.getSearchDepth() : "basic")
                .topic(request.getTopic() != null ? request.getTopic() : "general")
                .days(request.getDays() != null ? request.getDays() : 300)
                .maxResults(request.getMaxResults() != 0 ? request.getMaxResults() : 10)
                .includeImages(request.isIncludeImages()).includeImageDescriptions(request.isIncludeImageDescriptions())
                .includeAnswer(request.isIncludeAnswer()).includeRawContent(request.isIncludeRawContent())
                .includeDomains(
                        request.getIncludeDomains() != null ? request.getIncludeDomains() : Collections.emptyList())
                .excludeDomains(
                        request.getExcludeDomains() != null ? request.getExcludeDomains() : Collections.emptyList())
                .build();

        log.debug("Sending request to Tavily API: query={}, searchDepth={}, topic={}, days={}, maxResults={}",
                requestWithApiKey.getQuery(), requestWithApiKey.getSearchDepth(), requestWithApiKey.getTopic(),
                requestWithApiKey.getDays(), requestWithApiKey.getMaxResults());

        try {
            TavilyResponse response = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/search").build())
                    .bodyValue(requestWithApiKey)
                    .retrieve()
                    .bodyToMono(TavilyResponse.class)
                    .block();

            log.info("Received response from Tavily API for query: {}", requestWithApiKey.getQuery());
            return response;
        } catch (Exception e) {
            log.error("Error occurred while calling Tavily API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch search results from Tavily API", e);
        }
    }
}
