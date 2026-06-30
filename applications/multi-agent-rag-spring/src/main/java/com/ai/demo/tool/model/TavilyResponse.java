package com.ai.demo.tool.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response object for the Tavily API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonClassDescription("Response object for the Tavily API")
public class TavilyResponse {
    @JsonProperty("query")
    private String query;

    @JsonProperty("follow_up_questions")
    private List<String> followUpQuestions;

    @JsonProperty("answer")
    private String answer;

    @JsonDeserialize(using = ImageDeserializer.class)
    @JsonProperty("images")
    private List<Image> images;

    @JsonProperty("results")
    private List<Result> results;

    @JsonProperty("response_time")
    private float responseTime;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Image {
        @JsonProperty("url")
        private String url;

        @JsonProperty("description")
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        @JsonProperty("title")
        private String title;

        @JsonProperty("url")
        private String url;

        @JsonProperty("content")
        private String content;

        @JsonProperty("raw_content")
        private String rawContent;

        @JsonProperty("score")
        private float score;

        @JsonProperty("published_date")
        private String publishedDate;
    }
}
