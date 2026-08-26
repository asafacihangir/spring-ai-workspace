package org.phoenix.coordinator.analyze;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AnalyzeController {

    private final AnalyzeService analyzeService;

    public AnalyzeController(AnalyzeService analyzeService) {
        this.analyzeService = analyzeService;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.TEXT_PLAIN_VALUE, produces = "text/markdown")
    public String analyze(@RequestBody(required = false) String ticket) {
        if (!StringUtils.hasText(ticket)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ticket body must not be blank");
        }
        return analyzeService.analyze(ticket);
    }
}
