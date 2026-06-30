package com.ai.demo.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/graph")
@Slf4j
public class GraphController {

    @Value("classpath:documents/faq.md")
    Resource faqDocument;

    @Value("classpath:documents/overview.md")
    Resource overviewDocument;

    private final CompiledGraph compiledGraph;

    private final VectorStore vectorStore;

    private final String SAVE_PATH = System.getProperty("user.dir") + "/src/main/resources/vectorstore/vectorstore.json";

    @SneakyThrows
    public GraphController(@Qualifier("graph") StateGraph stateGraph, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.compiledGraph = stateGraph.compile();
    }

    @GetMapping(value = "/add")
    public void addDocuments() {
        File file = new File(SAVE_PATH);
        if (file.exists()) {
            log.info("Vector store already exists at {}, skipping document loading", SAVE_PATH);
            return;
        }

        log.info("Creating vector store from markdown documents");
        var markdownReader1 = new MarkdownDocumentReader(faqDocument, MarkdownDocumentReaderConfig.builder()
                .withAdditionalMetadata("title", "Spring AI Alibaba FAQ")
                .withAdditionalMetadata("summary", "Frequently asked questions and answers about Spring AI Alibaba")
                .build());
        List<Document> documents = new ArrayList<>(markdownReader1.get());

        var markdownReader2 = new MarkdownDocumentReader(overviewDocument, MarkdownDocumentReaderConfig.builder()
                .withAdditionalMetadata("title", "Spring AI Alibaba Overview")
                .withAdditionalMetadata("summary", "Overview of Spring AI Alibaba")
                .build());
        documents.addAll(markdownReader2.get());

        // Add documents to the vector store
        vectorStore.add(documents);

        // Persist
        file.getParentFile().mkdirs();
        ((SimpleVectorStore) vectorStore).save(file);
        log.info("Vector store saved to {}", SAVE_PATH);
    }

    @GetMapping(value = "/chat")
    public Map<String, Object> chat(@RequestParam(value = "query", defaultValue = "Hello, I would like to know about large language models",
            required = false) String query) {
        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId(UUID.randomUUID().toString())
                .build();
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("question", query);
        Optional<OverAllState> invoke = this.compiledGraph.invoke(objectMap, runnableConfig);
        return invoke.map(OverAllState::data).orElse(new HashMap<>());
    }
}
