package org.phoenix.repoagent;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.ai.openai.api-key=test",
        "repo.agent.name=t",
        "repo.agent.description=t",
        "repo.agent.root=.",
        "server.port=0"
})
class RepoAgentApplicationTests {

    @Test
    void contextLoads() {
    }

}
