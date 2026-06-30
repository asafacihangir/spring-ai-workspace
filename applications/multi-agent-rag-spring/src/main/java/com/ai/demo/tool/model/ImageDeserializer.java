package com.ai.demo.tool.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageDeserializer extends JsonDeserializer<List<TavilyResponse.Image>> {
    @Override
    public List<TavilyResponse.Image> deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        List<TavilyResponse.Image> images = new ArrayList<>();

        if (node.isArray()) {
            for (JsonNode element : node) {
                // If element is a string, treat it as a URL
                if (element.isTextual()) {
                    images.add(new TavilyResponse.Image(element.asText(), null));
                }
                // If element is an object, map it to Image
                else if (element.isObject()) {
                    String url = element.has("url") ? element.get("url").asText() : null;
                    String description = element.has("description") ? element.get("description").asText() : null;
                    images.add(new TavilyResponse.Image(url, description));
                }
            }
        }

        return images;
    }
}
