package com.ai.demo.edge;

import com.ai.demo.entity.RouteQueryEntity;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RouteQuestionEdge implements EdgeAction {

    private final ChatClient questionRouterChatClient;

    public RouteQuestionEdge(@Qualifier("QuestionRouterChatClient") ChatClient questionRouterChatClient) {
        this.questionRouterChatClient = questionRouterChatClient;
    }

    @Override
    public String apply(OverAllState state) {
        log.info("---------- Edge: Route Question ----------");

        String question = state.value("question", String.class).orElse("");

        // Decide data source
        RouteQueryEntity response = questionRouterChatClient.prompt()
                .user(u -> u.param("question", question))
                .system(s -> s.param("knowledge_base", "Spring AI Alibaba related knowledge"))
                .call()
                .entity(RouteQueryEntity.class);

        if (response == null) {
            log.warn("Route question returned null response, falling back to web_search");
            return "web_search";
        }
        log.info("Routed to: {}", response.dataSource());
        return response.dataSource();
    }
}
