package org.phoenix.repoagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RepoAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepoAgentApplication.class, args);
    }

}
