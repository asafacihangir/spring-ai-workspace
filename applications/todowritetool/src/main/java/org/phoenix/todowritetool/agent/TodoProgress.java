package org.phoenix.todowritetool.agent;

import org.springaicommunity.agent.tools.TodoWriteTool.Todos;
import org.springaicommunity.agent.tools.TodoWriteTool.Todos.Status;
import org.springaicommunity.agent.tools.TodoWriteTool.Todos.TodoItem;


public final class TodoProgress {

    private TodoProgress() {
    }

    static String mark(Status status) {
        return switch (status) {
            case completed -> "✓";
            case in_progress -> "▶";
            case pending -> "☐";
        };
    }

    public static String format(Todos todos) {
        var items = todos.todos();
        if (items.isEmpty()) {
            return "(boş plan) — %0";
        }
        var sb = new StringBuilder();
        long done = 0;
        for (TodoItem item : items) {
            if (item.status() == Status.completed) {
                done++;
            }
            sb.append(mark(item.status())).append(' ').append(item.content()).append('\n');
        }
        int percent = (int) (done * 100 / items.size());
        sb.append("— %").append(percent);
        return sb.toString();
    }
}
