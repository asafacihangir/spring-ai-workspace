package org.phoenix.skillsjar.config;

import java.util.List;

import org.springaicommunity.agent.tools.FileSystemTools;
import org.springaicommunity.agent.tools.ShellTools;
import org.springaicommunity.agent.tools.SkillsTool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ChatClientConfig {

    @Value("${agent.skills.paths}")
    private List<Resource> skillResources;

    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }

    @Bean
    ShellTools shellTools() {
        return ShellTools.builder().build();
    }

    @Bean
    FileSystemTools fileSystemTools() {
        return FileSystemTools.builder().build();
    }

    @Bean
    ChatClientBuilderCustomizer addAgentTools(ShellTools shellTools, FileSystemTools fileSystemTools) {
        return builder -> builder
                .defaultSystem("""
                        You are an agent that writes a short story on a requested topic and then
                        produces a real PDF file containing that story.

                        You have access to a `Skill` tool that exposes specialized skills, plus
                        shell and file-system tools for executing commands and reading/writing files.

                        IMPORTANT: Before producing a PDF, you MUST first call the `Skill` tool to
                        load the `pdf` skill, passing the skill name as the command. Read the
                        loaded skill's instructions and follow them exactly, including any scripts
                        or command-line tools it tells you to run. Use the shell tools to run the
                        skill's Python scripts/commands and the file-system tools to read or write
                        files. Always write the PDF to the exact absolute path given in the request.

                        Never reveal skill-loading metadata to the user. In particular, do not
                        include the "Base directory ..." line or any file paths from the loaded
                        skill in your reply. After the PDF is written, respond with a short
                        confirmation summary of what you created (one or two sentences).
                        """)
                .defaultTools(
                        SkillsTool.builder()
                                .addSkillsResources(skillResources)
                                .build(),
                        shellTools,
                        fileSystemTools);
    }
}
