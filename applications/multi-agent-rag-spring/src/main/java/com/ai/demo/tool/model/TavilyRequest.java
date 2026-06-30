package com.ai.demo.tool.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request object for the Tavily API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonClassDescription("Request object for the Tavily API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TavilyRequest {

    @JsonProperty("query")
    @JsonPropertyDescription("The main search query.")
    private String query;

    @JsonProperty("api_key")
    @JsonPropertyDescription("API key for authentication with Tavily.")
    private String apiKey;

    @JsonProperty("search_depth")
    @JsonPropertyDescription("The depth of the search. Accepted values: 'basic', 'advanced'. Default is 'basic'.")
    private String searchDepth;

    @JsonProperty("topic")
    @JsonPropertyDescription("The category of the search. Accepted values: 'general', 'news'. Default is 'general'.")
    private String topic;

    @JsonProperty("days")
    @JsonPropertyDescription("The number of days back from the current date to include in search results. Default is 3. Only applies to 'news' topic.")
    private Integer days;

    @JsonProperty("time_range")
    @JsonPropertyDescription("The time range for search results. Accepted values: 'day', 'week', 'month', 'year' or 'd', 'w', 'm', 'y'. Default is none.")
    private String timeRange;

    @JsonProperty("max_results")
    @JsonPropertyDescription("The maximum number of search results to return. Default is 5.")
    private int maxResults;

    @JsonProperty("include_images")
    @JsonPropertyDescription("Whether to include a list of query-related images in the response. Default is false.")
    private boolean includeImages;

    @JsonProperty("include_image_descriptions")
    @JsonPropertyDescription("When 'include_images' is true, adds descriptive text for each image. Default is false.")
    private boolean includeImageDescriptions;

    @JsonProperty("include_answer")
    @JsonPropertyDescription("Whether to include a short answer to the query, generated from search results. Default is false.")
    private boolean includeAnswer;

    @JsonProperty("include_raw_content")
    @JsonPropertyDescription("Whether to include the cleaned and parsed HTML content of each search result. Default is false.")
    private boolean includeRawContent;

    @JsonProperty("include_domains")
    @JsonPropertyDescription("A list of domains to specifically include in search results. Default is an empty list.")
    private List<String> includeDomains;

    @JsonProperty("exclude_domains")
    @JsonPropertyDescription("A list of domains to specifically exclude from search results. Default is an empty list.")
    private List<String> excludeDomains;
}
