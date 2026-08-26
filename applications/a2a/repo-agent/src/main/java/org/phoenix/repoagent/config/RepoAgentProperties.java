package org.phoenix.repoagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "repo.agent")
public record RepoAgentProperties(String name, String description, String root) {
}
