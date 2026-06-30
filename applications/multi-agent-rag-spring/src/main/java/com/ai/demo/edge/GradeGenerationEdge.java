package com.ai.demo.edge;

import com.ai.demo.entity.GradeScore;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GradeGenerationEdge implements EdgeAction {

    private static final int MAX_RETRIES = 3;

    private static final String KEY_QUESTION = "question";
    private static final String KEY_GENERATION = "generation";
    private static final String KEY_DOCUMENTS = "documents";
    private static final String KEY_RETRY_COUNT = "retry_count";

    private static final String GRADE_USEFUL = "useful";
    private static final String GRADE_UNUSEFUL = "unuseful";
    private static final String GRADE_HALLUCINATION = "hallucination";

    private static final String SCORE_YES = "yes";

    private final ChatClient hallucinationGrader;

    private final ChatClient answerGrader;

    public GradeGenerationEdge(@Qualifier("HallucinationChatClient") ChatClient hallucinationGrader,
            @Qualifier("AnswerGraderChatClient") ChatClient answerGrader) {
        this.hallucinationGrader = hallucinationGrader;
        this.answerGrader = answerGrader;
    }

    /**
     * Evaluates generation quality.
     * @param state graph state
     * @return "hallucination" if the generated answer is not grounded in facts and needs retry;
     * "unuseful" if the generated answer does not address the question and needs query rewriting;
     * "useful" if the generated answer addresses the question.
     */
    @Override
    public String apply(OverAllState state) {
        log.info("---------- Edge: Check if the generated answer is grounded in facts ----------");
        String question = state.value(KEY_QUESTION, String.class).orElse("");
        String generation = state.value(KEY_GENERATION, String.class).orElse("");
        List<Document> documents = state.value(KEY_DOCUMENTS, List.of());
        int retryCount = state.value(KEY_RETRY_COUNT, Integer.class).orElse(0) + 1;

        Optional<String> circuitBreakerResult = checkCircuitBreaker(retryCount);
        if (circuitBreakerResult.isPresent()) {
            return circuitBreakerResult.get();
        }

        if (!gradeHallucination(documents, generation, retryCount)) {
            state.data().put(KEY_RETRY_COUNT, retryCount);
            return GRADE_HALLUCINATION;
        }

        if (gradeAnswer(question, generation)) {
            return GRADE_USEFUL;
        }

        state.data().put(KEY_RETRY_COUNT, retryCount);
        return GRADE_UNUSEFUL;
    }

    private Optional<String> checkCircuitBreaker(int retryCount) {
        if (retryCount > MAX_RETRIES) {
            log.warn("---------- Circuit breaker: max retries ({}) reached, accepting current answer ----------", MAX_RETRIES);
            return Optional.of(GRADE_USEFUL);
        }
        return Optional.empty();
    }

    private boolean gradeHallucination(List<Document> documents, String generation, int retryCount) {
        GradeScore score = hallucinationGrader.prompt()
                .user(u -> u.param("documents", formatDocs(documents))
                        .param("generation", generation))
                .call()
                .entity(GradeScore.class);

        if (score == null || !SCORE_YES.equals(score.binaryScore())) {
            log.info("---------- Decision: Generated answer is not grounded in facts, retrying (attempt {}/{}) ----------",
                    retryCount, MAX_RETRIES);
            return false;
        }

        log.info("---------- Decision: Generated answer is grounded in facts ----------");
        return true;
    }

    private boolean gradeAnswer(String question, String generation) {
        GradeScore score = answerGrader.prompt()
                .user(u -> u.param("question", question)
                        .param("generation", generation))
                .call()
                .entity(GradeScore.class);

        if (score != null && SCORE_YES.equals(score.binaryScore())) {
            log.info("---------- Decision: Generated answer addresses the question ----------");
            return true;
        }

        log.info("---------- Decision: Generated answer does not address the question, rewriting ----------");
        return false;
    }

    private String formatDocs(List<Document> documents) {
        return documents.stream().map(Document::getText).collect(Collectors.joining("\n\n"));
    }
}
