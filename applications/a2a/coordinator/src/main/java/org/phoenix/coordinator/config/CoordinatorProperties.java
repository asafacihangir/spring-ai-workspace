package org.phoenix.coordinator.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "coordinator")
public record CoordinatorProperties(List<String> repoAgentUrls, String agentsDir, String memoryDir) {
}
