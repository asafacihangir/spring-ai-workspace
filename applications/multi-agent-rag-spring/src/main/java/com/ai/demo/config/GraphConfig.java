package com.ai.demo.config;

import com.ai.demo.edge.GradeGenerationEdge;
import com.ai.demo.edge.RouteQuestionEdge;
import com.ai.demo.node.GenerationNode;
import com.ai.demo.node.RetrieveNode;
import com.ai.demo.node.TransformQueryNode;
import com.ai.demo.node.WebSearchNode;
import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@Slf4j
public class GraphConfig {

    // Node names
    private static final String NODE_PREBUILT_RAG = "prebuilt_rag_generation";
    private static final String NODE_WEB_SEARCH = "web_search";
    private static final String NODE_SELF_RAG = "self_rag_generation";
    private static final String NODE_TRANSFORM_QUERY = "transform_query";

    // State keys
    private static final String KEY_QUESTION = "question";
    private static final String KEY_GENERATION = "generation";
    private static final String KEY_DOCUMENTS = "documents";
    private static final String KEY_RETRY_COUNT = "retry_count";

    // Edge results
    private static final String ROUTE_VECTORSTORE = "vectorstore";
    private static final String ROUTE_WEB_SEARCH = "web_search";
    private static final String GRADE_USEFUL = "useful";
    private static final String GRADE_UNUSEFUL = "unuseful";
    private static final String GRADE_HALLUCINATION = "hallucination";

    private final RouteQuestionEdge routeQuestionEdge;

    private final GradeGenerationEdge gradeGenerationEdge;

    private final ChatClient commonChatClient;

    private final ChatClient webSearchClient;

    private final ChatClient ragChatClient;

    private final ChatClient questionRewriterChatClient;

    private final DocumentRetriever documentRetriever;

    private final CompressionQueryTransformer compressionQueryTransformer;

    private final RewriteQueryTransformer rewriteQueryTransformer;

    private final TranslationQueryTransformer translationQueryTransformer;

    public GraphConfig(RouteQuestionEdge routeQuestionEdge, GradeGenerationEdge gradeGenerationEdge,
            ChatClient commonChatClient,
            @Qualifier("WebSearchChatClient") ChatClient webSearchClient,
            @Qualifier("AdaptiveRagChatClient") ChatClient ragChatClient,
            @Qualifier("QuestionRewriterChatClient") ChatClient questionRewriterChatClient,
            DocumentRetriever documentRetriever,
            CompressionQueryTransformer compressionQueryTransformer,
            RewriteQueryTransformer rewriteQueryTransformer,
            TranslationQueryTransformer translationQueryTransformer) {
        this.routeQuestionEdge = routeQuestionEdge;
        this.gradeGenerationEdge = gradeGenerationEdge;
        this.commonChatClient = commonChatClient;
        this.webSearchClient = webSearchClient;
        this.ragChatClient = ragChatClient;
        this.questionRewriterChatClient = questionRewriterChatClient;
        this.documentRetriever = documentRetriever;
        this.compressionQueryTransformer = compressionQueryTransformer;
        this.rewriteQueryTransformer = rewriteQueryTransformer;
        this.translationQueryTransformer = translationQueryTransformer;
    }

    @Bean
    public StateGraph graph() throws GraphStateException {
        StateGraph stateGraph = new StateGraph("Spring AI Alibaba Graph Demo", createStateFactory());

        // Add nodes
        stateGraph.addNode(NODE_PREBUILT_RAG, createRetrieveNodeAction());
        stateGraph.addNode(NODE_WEB_SEARCH, createWebSearchNodeAction());
        stateGraph.addNode(NODE_SELF_RAG, createSelfRagGenerationNodeAction());
        stateGraph.addNode(NODE_TRANSFORM_QUERY, createTransformQueryNodeAction());

        // Decide whether to retrieve via vector store or web search
        stateGraph.addConditionalEdges(StateGraph.START, AsyncEdgeAction.edge_async(routeQuestionEdge),
                Map.of(ROUTE_VECTORSTORE, NODE_PREBUILT_RAG, ROUTE_WEB_SEARCH, NODE_WEB_SEARCH));

        // Vector store chains
        stateGraph.addConditionalEdges(NODE_PREBUILT_RAG, AsyncEdgeAction.edge_async(gradeGenerationEdge),
                Map.of(GRADE_USEFUL, StateGraph.END,
                        GRADE_UNUSEFUL, NODE_TRANSFORM_QUERY,
                        GRADE_HALLUCINATION, NODE_PREBUILT_RAG));

        // Web search chains
        stateGraph.addEdge(NODE_WEB_SEARCH, NODE_SELF_RAG);
        stateGraph.addConditionalEdges(NODE_SELF_RAG, AsyncEdgeAction.edge_async(gradeGenerationEdge),
                Map.of(GRADE_USEFUL, StateGraph.END,
                        GRADE_UNUSEFUL, NODE_TRANSFORM_QUERY,
                        GRADE_HALLUCINATION, NODE_SELF_RAG));

        // Rewrite question
        stateGraph.addEdge(NODE_TRANSFORM_QUERY, NODE_SELF_RAG);

        logGraphRepresentation(stateGraph);

        return stateGraph;
    }

    private OverAllStateFactory createStateFactory() {
        return () -> {
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy(KEY_QUESTION, new ReplaceStrategy());
            state.registerKeyAndStrategy(KEY_GENERATION, new ReplaceStrategy());
            state.registerKeyAndStrategy(KEY_DOCUMENTS, new ReplaceStrategy());
            state.registerKeyAndStrategy(KEY_RETRY_COUNT, new ReplaceStrategy());
            return state;
        };
    }

    private AsyncNodeAction createRetrieveNodeAction() {
        RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryTransformers(compressionQueryTransformer, translationQueryTransformer, rewriteQueryTransformer)
                .build();

        return AsyncNodeAction.node_async(RetrieveNode.builder()
                .chatClient(commonChatClient)
                .documentRetriever(documentRetriever)
                .retrievalAugmentationAdvisor(advisor)
                .build());
    }

    private AsyncNodeAction createWebSearchNodeAction() {
        return AsyncNodeAction.node_async(WebSearchNode.builder()
                .chatClient(webSearchClient)
                .build());
    }

    private AsyncNodeAction createSelfRagGenerationNodeAction() {
        return AsyncNodeAction.node_async(GenerationNode.builder()
                .chatClient(ragChatClient)
                .build());
    }

    private AsyncNodeAction createTransformQueryNodeAction() {
        return AsyncNodeAction.node_async(TransformQueryNode.builder()
                .chatClient(questionRewriterChatClient)
                .build());
    }

    private void logGraphRepresentation(StateGraph stateGraph) {
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.MERMAID,
                "Adaptive rag flow");
        log.info("\n=== Adaptive rag Flow ===");
        log.info(representation.content());
        log.info("==================================\n");
    }
}
