package org.phoenix.skills.tools;

import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;


@Service
public class WeatherTools {

    private static final Weather DEFAULT = new Weather("00000", "Clear", "72F");

    private static final Map<String, Weather> WEATHER_BY_ZIPCODE = Map.of(
            "10001", new Weather("10001", "Sunny", "78F"),
            "33101", new Weather("33101", "Humid", "88F"),
            "92101", new Weather("92101", "Breezy", "70F"),
            "78701", new Weather("78701", "Sunny", "84F"),
            "92802", new Weather("92802", "Clear", "75F")
    );

    @Tool(name = "get-weather-for-zipcode",
            description = "Gets the current weather for a given US zipcode")
    public Weather getWeatherForZipcode(String zipcode) {
        return WEATHER_BY_ZIPCODE.getOrDefault(zipcode, new Weather(zipcode, DEFAULT.conditions(), DEFAULT.temperature()));
    }
}
