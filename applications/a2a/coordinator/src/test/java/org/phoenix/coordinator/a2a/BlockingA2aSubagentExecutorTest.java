package org.phoenix.coordinator.a2a;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockingA2aSubagentExecutorTest {

    private final ObjectMapper json = new ObjectMapper();
    private final BlockingA2aSubagentExecutor executor = new BlockingA2aSubagentExecutor();

    @Test
    void whenResultIsCompletedTask_returnsConcatenatedArtifactText() throws Exception {
        String body = """
                {"jsonrpc":"2.0","id":"1","result":{"kind":"task",
                 "status":{"state":"completed"},
                 "artifacts":[{"parts":[{"kind":"text","text":"Hello "},{"kind":"text","text":"world"}]}]}}
                """;

        assertEquals("Hello world", executor.extractResult(json.readTree(body)));
    }

    @Test
    void whenResultIsDirectMessage_returnsMessageText() throws Exception {
        String body = """
                {"jsonrpc":"2.0","id":"1","result":{"kind":"message",
                 "parts":[{"kind":"text","text":"direct answer"}]}}
                """;

        assertEquals("direct answer", executor.extractResult(json.readTree(body)));
    }

    @Test
    void whenTaskEndedInNonCompletedState_saysSoInsteadOfReturningEmpty() throws Exception {
        String body = """
                {"jsonrpc":"2.0","id":"1","result":{"kind":"task","status":{"state":"failed"}}}
                """;

        String result = executor.extractResult(json.readTree(body));

        assertTrue(result.contains("failed"), result);
    }

    @Test
    void whenResponseIsJsonRpcError_throwsWithErrorMessage() throws Exception {
        String body = """
                {"jsonrpc":"2.0","id":"1","error":{"code":-32600,"message":"boom"}}
                """;

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> executor.extractResult(json.readTree(body)));

        assertTrue(e.getMessage().contains("boom"), e.getMessage());
    }

}
