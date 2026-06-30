package org.phoenix.structuredoutput.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.util.Assert;


public class LenientJsonOutputConverter<T> implements StructuredOutputConverter<T> {


    private static final Pattern FENCE = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```");

    private final BeanOutputConverter<T> delegate;

    public LenientJsonOutputConverter(Class<T> targetType) {
        Assert.notNull(targetType, "targetType must not be null");
        this.delegate = new BeanOutputConverter<>(targetType);
    }


    @Override
    public String getFormat() {
        return this.delegate.getFormat();
    }


    public String getJsonSchema() {
        return this.delegate.getJsonSchema();
    }


    @Override
    public T convert(String source) {
        Assert.hasText(source,
                "Source text to convert must not be null or blank; a valid response was expected from the LLM");
        Matcher matcher = FENCE.matcher(source);
        String json = matcher.find() ? matcher.group(1).trim() : source.trim();
        return this.delegate.convert(json);
    }
}
