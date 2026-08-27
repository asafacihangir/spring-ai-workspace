package org.phoenix.demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class App01Application {

    public static void main(String[] args) {
        SpringApplication.run(App01Application.class, args);
    }

    @Bean
    ApplicationRunner go(ChatClient chatClient) {
        return args -> {

            var answer = chatClient.prompt()
                    .system("""
              You are a Disney parks assistant.
              When the pick-next-ride tool returns an attraction, that attraction IS the answer.
              State it in one sentence with its wait time. Do not offer alternatives
              and do not ask follow-up questions.
              """)
                    .user("""
              I'm in New Orleans Square.
              Should I ride Pirates of the Caribbean, Haunted Mansion, Star Tours,
              or Rise of the Resistance next?
              """)
                    .call()
                    .content();

            System.err.println(" --> " + answer);
        };
    }


}
