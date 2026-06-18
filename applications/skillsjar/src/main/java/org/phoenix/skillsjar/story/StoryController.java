package org.phoenix.skillsjar.story;

import org.phoenix.skillsjar.story.StoryService.StoryResult;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @PostMapping("/stories")
    public StoryResponse create(@RequestBody StoryRequest request) {
        StoryResult result = storyService.generate(request.topic());
        return new StoryResponse(result.pdfPath(), result.summary());
    }

    public record StoryRequest(String topic) {
    }

    public record StoryResponse(String pdfPath, String summary) {
    }
}
