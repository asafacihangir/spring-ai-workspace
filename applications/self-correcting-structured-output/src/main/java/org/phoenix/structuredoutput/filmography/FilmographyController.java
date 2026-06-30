package org.phoenix.structuredoutput.filmography;

import org.phoenix.structuredoutput.model.ActorFilmography;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/filmography")
public class FilmographyController {

    private final FilmographyService filmographyService;

    public FilmographyController(FilmographyService filmographyService) {
        this.filmographyService = filmographyService;
    }

    @PostMapping("/plain")
    public ActorFilmography plain(@RequestBody FilmographyRequest request) {
        return filmographyService.plain(request.prompt());
    }

    @PostMapping("/validated")
    public ActorFilmography validated(@RequestBody FilmographyRequest request) {
        return filmographyService.validated(request.prompt());
    }

    @PostMapping("/provider")
    public ActorFilmography provider(@RequestBody FilmographyRequest request) {
        return filmographyService.provider(request.prompt());
    }

    @PostMapping("/lenient")
    public ActorFilmography lenient(@RequestBody FilmographyRequest request) {
        return filmographyService.lenient(request.prompt());
    }

    @PostMapping("/full")
    public FilmographyFullResponse full(@RequestBody FilmographyRequest request) {
        ResponseEntity<ChatResponse, ActorFilmography> result = filmographyService.full(request.prompt());
        long totalTokens = result.response().getMetadata().getUsage().getTotalTokens();
        return new FilmographyFullResponse(result.entity(), totalTokens);
    }

    public record FilmographyRequest(String prompt) {
    }

    public record FilmographyFullResponse(ActorFilmography entity, long totalTokens) {
    }
}
