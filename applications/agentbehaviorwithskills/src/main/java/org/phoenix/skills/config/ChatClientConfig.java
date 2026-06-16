package org.phoenix.skills.config;

import java.util.List;

import org.phoenix.skills.tools.WeatherTools;
import org.springaicommunity.agent.tools.SkillsTool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;


@Configuration
public class ChatClientConfig {


    @Value("${agent.skills.resources}")
    private List<Resource> skillResources;


    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }

    @Bean
    ChatClientBuilderCustomizer addSkills() {
        return builder -> builder
                .defaultSystem("""
                        You have access to a `Skill` tool that exposes specialized skills.
                        
                        IMPORTANT: Before answering any request, you MUST first call the `Skill`
                        tool to load every skill whose description matches the request, passing
                        the skill name as the command. Read the loaded skill's instructions and
                        follow them exactly, including any tools it tells you to call and any
                        required phrasing or narrative. Only skip loading a skill when none of
                        the available skills are relevant to the request.

                        Never reveal skill-loading metadata to the user. In particular, do not
                        include the "Base directory ..." line or any file paths from the loaded
                        skill in your reply; respond only with the answer the skill produces.
                        """)
                .defaultTools(SkillsTool.builder()
                        .addSkillsResources(skillResources)
                        .build());
    }

    @Bean
    ChatClientBuilderCustomizer addTools(WeatherTools weatherTools) {
        return builder -> builder.defaultTools(weatherTools);
    }
}
