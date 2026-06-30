package com.example.docling.chat.application;

import com.example.docling.chat.domain.ChatResponse;
import com.example.docling.chat.domain.UserQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class ProcessQueryUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessQueryUseCase.class);

    private final ChatClient chatClient;

    public ProcessQueryUseCase(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(RetrievalAugmentationAdvisor.builder()
                        .documentRetriever(VectorStoreDocumentRetriever.builder()
                                .vectorStore(vectorStore)
                                .build())
                        .build())
                .build();
    }

    public ChatResponse execute(UserQuestion question) {
        log.info("Processing RAG query: {}", question.text());

        String answer = chatClient
                .prompt(question.text())
                .call()
                .content();

        log.info("RAG query processed successfully");
        return ChatResponse.of(answer);
    }
}
