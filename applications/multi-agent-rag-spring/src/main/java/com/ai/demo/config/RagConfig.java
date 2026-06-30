package com.ai.demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RagConfig {

    private static final String VECTORSTORE_PATH = System.getProperty("user.dir")
            + "/src/main/resources/vectorstore/vectorstore.json";

    /**
     * Text splitter for splitting documents into smaller chunks.
     * @return TextSplitter instance
     */
    @Bean
    TextSplitter textSplitter() {
        return TokenTextSplitter.builder()
                // Can further configure max token count per chunk, split window size, etc.
                .build();
    }

    /**
     * Vector store for storing vector representations of document chunks.
     * @return VectorStore instance
     */
    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        File file = new File(VECTORSTORE_PATH);
        if (file.exists()) {
            log.info("Auto-loading vector store from {}", VECTORSTORE_PATH);
            store.load(file);
        }
        return store;
    }

    /**
     * Document retriever for retrieving relevant document chunks from the vector store.
     * @param vectorStore vector store
     * @return DocumentRetriever instance
     */
    @Bean
    DocumentRetriever documentRetriever(VectorStore vectorStore) {
        return VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.50)
                .build();
    }

    /**
     * Compression query transformer: compresses conversation history and follow-up queries into a standalone query that captures the essence of the conversation.
     * <p><em>Pre-retrieval enhancement</em> — useful when conversation history is long and follow-up queries are related to the conversation context.</p>
     * @param chatClient chat model
     * @return CompressionQueryTransformer instance
     */
    @Bean
    CompressionQueryTransformer compressionQueryTransformer(ChatClient chatClient) {
        return CompressionQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())
                .build();
    }

    /**
     * Rewrite query transformer: rewrites user queries.
     * <p><em>Pre-retrieval enhancement</em> — useful when user queries are verbose, ambiguous, or contain irrelevant information that may affect search result quality.</p>
     * @param chatClient chat model
     * @return RewriteQueryTransformer instance
     */
    @Bean
    RewriteQueryTransformer rewriteQueryTransformer(ChatClient chatClient) {
        return RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())
                .build();
    }

    /**
     * Translation query transformer: translates user queries to the target language.
     * <p><em>Pre-retrieval enhancement</em> — useful when user queries are in a language different from the target language.</p>
     * @param chatClient chat model
     * @return TranslationQueryTransformer instance
     */
    @Bean
    TranslationQueryTransformer translationQueryTransformer(ChatClient chatClient) {
        return TranslationQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())
                .targetLanguage("english")
                .build();
    }

    /**
     * Multi-query expander: generates multiple queries to improve retrieval coverage.
     * <p><em>Pre-retrieval enhancement</em> — leverages the LLM to generate multi-semantic query statements from different perspectives.</p>
     * @param chatClient chat model
     * @return MultiQueryExpander instance
     */
    @Bean
    MultiQueryExpander multiQueryExpander(ChatClient chatClient) {
        return MultiQueryExpander.builder()
                .chatClientBuilder(chatClient.mutate())
                .numberOfQueries(3)
                .build();
    }
}
