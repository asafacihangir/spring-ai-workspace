package org.phoenix.todowritetool.agent;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springaicommunity.agent.tools.TodoWriteTool.Todos;
import org.springaicommunity.agent.tools.TodoWriteTool.Todos.Status;
import org.springaicommunity.agent.tools.TodoWriteTool.Todos.TodoItem;

import static org.assertj.core.api.Assertions.assertThat;

class TodoProgressTest {

    @Test
    void formatsMixedStatusesWithMarksAndPercent() {
        Todos todos = new Todos(List.of(
                new TodoItem("Determine criteria", Status.completed, "Determining criteria"),
                new TodoItem("Analyze Apollo 11", Status.in_progress, "Analyzing Apollo 11"),
                new TodoItem("Compare", Status.pending, "Comparing"),
                new TodoItem("Write report", Status.pending, "Writing report")));

        String out = TodoProgress.format(todos);

        assertThat(out).contains("✓ Determine criteria");
        assertThat(out).contains("▶ Analyze Apollo 11");
        assertThat(out).contains("☐ Compare");
        assertThat(out).contains("%25"); // 1/4 completed
    }

    @Test
    void emptyPlanIsZeroPercent() {
        assertThat(TodoProgress.format(new Todos(List.of()))).contains("%0");
    }
}
