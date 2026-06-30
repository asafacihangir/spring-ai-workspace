package org.phoenix.structuredoutput.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;


public record ActorFilmography(

        @JsonPropertyDescription("The actor's full name (first and last), must not be empty.")
        String actor,

        @JsonPropertyDescription("The actor's birth year, must be a reasonable value between 1850 and 2010.")
        int birthYear,

        @JsonPropertyDescription("The list of films the actor appeared in, must contain at least one film.")
        List<Film> films

) {
}
