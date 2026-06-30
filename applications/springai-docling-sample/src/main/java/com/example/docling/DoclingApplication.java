package com.example.docling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring AI + Docling RAG System Application.
 *
 * @see docs/architecture_rules.md
 */
@SpringBootApplication
public class DoclingApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoclingApplication.class, args);
    }
}
