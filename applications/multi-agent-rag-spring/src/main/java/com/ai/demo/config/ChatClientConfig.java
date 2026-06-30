package com.ai.demo.config;

import com.ai.demo.tool.WebSearchTool;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@AllArgsConstructor
public class ChatClientConfig {

    private final WebSearchTool webSearchTool;

    /**
     * Memory type: fixed-capacity message window.
     * <p>This is the default message type used when Spring AI auto-configures the ChatMemory bean (works without explicit configuration).</p>
     * @return ChatMemory instance
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();
    }

    /**
     * Memory storage: in-memory ChatMemoryRepository.
     * <p>By default, if no other Repository is configured, Spring AI will auto-configure an InMemoryChatMemoryRepository bean for direct use (works without explicit configuration).</p>
     * @return ChatMemoryRepository instance
     */
    @Bean
    public ChatMemoryRepository chatMemoryRepository() {
        return new InMemoryChatMemoryRepository();
    }

    /**
     * General-purpose OpenAI LLM client.
     * @param chatModel model configuration
     * @return ChatClient instance
     */
    @Bean
    @Primary
    public ChatClient openAiChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).defaultOptions(ChatOptions.builder().temperature(0.8).build()).build();
    }

    /**
     * Question Router ChatClient.
     * Routes user questions to different data sources (vector database or web search).
     * @param chatModel model configuration
     * @return ChatClient instance
     */
    @Bean
    public ChatClient QuestionRouterChatClient(ChatModel chatModel, ChatMemory chatMemory) {
        String systemPrompt = """
                    You are a routing expert responsible for directing user inputs/questions to the appropriate component:

                    1. Vector Store (vectorstore)
                    Choose vectorstore when the user's question is related to the document content in the knowledge base.

                    Knowledge base information:
                    {knowledge_base}

                    2. Web Search (web_search)
                    Choose web_search when the user's question involves:
                    - The need for real-time information (e.g., news, weather, stock prices, etc.)
                    - Topics outside the scope of the knowledge base

                    Please make the best routing decision.
                """;
        return ChatClient.builder(chatModel).defaultSystem(systemPrompt)
                .defaultUser(u -> u.text("User question: {question}"))
                .defaultOptions(ChatOptions.builder().temperature(0.0).build())
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId("QuestionRouter").build())
                .build();
    }

    /**
     * Web Search ChatClient.
     * Uses tools to perform web searches and return relevant information.
     * @param chatModel model configuration
     * @return ChatClient instance
     */
    @Bean
    public ChatClient WebSearchChatClient(ChatModel chatModel, ChatMemory chatMemory) {

        String systemPrompt = """
               Based on the user's question, use tools to perform a web search and return relevant information.

               Today's date is: {date}
               """;

        return ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .defaultUser(u -> u.text("User question: {question}"))
                .defaultOptions(ToolCallingChatOptions.builder().temperature(0.8).build())
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId("WebSearch").build())
                .defaultTools(webSearchTool)
                .build();
    }

    /**
     * Adaptive RAG ChatClient.
     * Combines vector database and web search results to answer user questions.
     * @param chatModel model configuration
     * @return ChatClient instance
     */
    @Bean
    public ChatClient AdaptiveRagChatClient(ChatModel chatModel, ChatMemory chatMemory) {
        String systemPrompt = """
               You are a professional Q&A assistant. Answer user questions based on the provided context information.

               Answer guidelines:
               1. Prioritize using the provided context information to answer questions
               2. Reference document titles and descriptions to understand the content background
               3. If multiple documents are relevant, synthesize information from multiple sources
               4. Keep answers accurate and concise, typically 2-3 sentences
               5. Mention information sources when appropriate
               6. If you are unsure or do not know the answer, honestly state so
               """;

        String userPrompt = """
                Question:
                {question}

                Retrieved context:
                {context}

                Please answer the question based on the above information.
                """;

        return ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .defaultUser(userPrompt)
                .defaultOptions(ChatOptions.builder().temperature(0.7).build())
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId("AdaptiveRag").build())
                .build();
    }

    /**
     * Evaluates whether the LLM-generated answer is grounded in the retrieved facts.
     * @param chatModel model configuration
     * @return ChatClient instance
     */
    @Bean
    public ChatClient HallucinationChatClient(ChatModel chatModel, ChatMemory chatMemory) {

        String systemPrompt = """
                You are a grader responsible for evaluating whether an LLM-generated answer is grounded in / supported by a set of retrieved facts.

                Provide a binary score of 'yes' or 'no'. 'yes' means the answer is grounded in / supported by the set of facts.
                """;

        String userPrompt = """
                Set of facts:
                {documents}

                LLM-generated answer:
                {generation}
                """;

        return ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .defaultUser(userPrompt)
                .defaultOptions(ChatOptions.builder().temperature(0.0).build())
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId("Hallucination").build())
                .build();
    }

    /**
     * Evaluates whether the LLM-generated answer addresses / resolves the user's question.
     * @param chatModel model configuration
     * @return ChatClient instance
     */
    @Bean
    public ChatClient AnswerGraderChatClient(ChatModel chatModel, ChatMemory chatMemory) {

        String systemPrompt = """
                You are a grader responsible for evaluating whether an answer addresses / resolves a question.

                Provide a binary score of 'yes' or 'no'. 'yes' means the answer addresses / resolves the question.
                """;

        String userPrompt = """
                User question:
                {question}

                LLM-generated answer:
                {generation}
                """;

        return ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .defaultUser(userPrompt)
                .defaultOptions(ChatOptions.builder().temperature(0.0).build())
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId("AnswerGrader").build())
                .build();
    }

    /**
     * Question Rewriter ChatClient.
     * Rewrites user questions into clearer, more specific forms to optimize retrieval.
     * @param chatModel model configuration
     * @return ChatClient instance
     */
    @Bean
    public ChatClient QuestionRewriterChatClient(ChatModel chatModel, ChatMemory chatMemory) {

        String systemPrompt = """
                You are a professional question rewriting expert responsible for rewriting user questions into clearer, more specific forms to optimize retrieval.

                Rewriting rules:
                1. Preserve the original meaning, but make the question more explicit
                2. Use more specific, targeted keywords and avoid vague or ambiguous phrasing
                3. If the question is too broad, try to refine it into more specific sub-questions
                """;

        String userPrompt = """
                Original question:
                {question}

                Please rewrite this question to improve retrieval effectiveness:
                """;

        return ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .defaultUser(userPrompt)
                .defaultOptions(ChatOptions.builder().temperature(0.0).build())
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId("QuestionRewriter").build())
                .build();
    }
}
