package org.phoenix.structuredoutput.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record Film(

        @JsonPropertyDescription("The full title of the film, must not be empty.")
        String title,

        @JsonPropertyDescription("The film's release year, must be greater than 1888 (the year of the first film).")
        int year

) {
}
