package org.phoenix.structuredoutput.filmography;

import org.phoenix.structuredoutput.converter.LenientJsonOutputConverter;
import org.phoenix.structuredoutput.model.ActorFilmography;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

@Service
public class FilmographyService {

    private final ChatClient chatClient;

    public FilmographyService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public ActorFilmography plain(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(ActorFilmography.class);
    }

    public ActorFilmography validated(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(ActorFilmography.class, ChatClient.EntityParamSpec::validateSchema);
    }

    public ActorFilmography provider(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(ActorFilmography.class, ChatClient.EntityParamSpec::useProviderStructuredOutput);
    }

    public ActorFilmography lenient(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(new LenientJsonOutputConverter<>(ActorFilmography.class));
    }

    public ResponseEntity<ChatResponse, ActorFilmography> full(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .responseEntity(ActorFilmography.class);
    }
}
