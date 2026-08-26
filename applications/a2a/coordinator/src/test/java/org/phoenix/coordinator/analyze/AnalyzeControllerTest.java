package org.phoenix.coordinator.analyze;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnalyzeControllerTest {

    private final AnalyzeController controller = new AnalyzeController(null);

    @Test
    void whenTicketBodyIsNull_respondsWith400() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> controller.analyze(null));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
    }

    @Test
    void whenTicketBodyIsBlank_respondsWith400() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> controller.analyze("   "));

        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
    }

}
