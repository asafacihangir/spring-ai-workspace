package org.phoenix.blogagent.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import org.phoenix.blogagent.config.BlogAgentProperties;
import org.phoenix.blogagent.model.BlogDraft;
import org.phoenix.blogagent.model.BlogOutline;
import org.phoenix.blogagent.model.ReviewedPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Agent(description = "Write and review a blog post about a given topic")
public class BlogWriterAgent {

    private static final Logger log = LoggerFactory.getLogger(BlogWriterAgent.class);

    private final BlogAgentProperties properties;

    public BlogWriterAgent(BlogAgentProperties properties) {
        this.properties = properties;
    }

    @Action(description = "Create an outline for the blog post")
    public BlogOutline createOutline(UserInput userInput, Ai ai) {
        return ai
                .withDefaultLlm()
                .withId("blog-post-outliner")
                .withPromptContributor(Personas.WRITER)
                .creating(BlogOutline.class)
                .fromPrompt("""
                        Create a blog post outline about: %s

                        Return a title and a list of section headings.
                        Keep it focused — aim for 4 to 6 sections.
                        Each section heading should be a short, clear phrase.
                        """.formatted(userInput.getContent())
                );
    }

    @Action(description = "Write a first draft of the blog post from an outline")
    public BlogDraft writeDraft(BlogOutline outline, Ai ai) {
        String sectionsText = String.join("\n- ", outline.sections());
        return ai
                .withDefaultLlm()
                .withId("blog-post-draft-writer")
                .withPromptContributor(Personas.WRITER)
                .creating(BlogDraft.class)
                .fromPrompt("""
                        You are a software developer and educator writing a blog post.
                        Write a blog post based on this outline:

                        Title: %s
                        Sections:
                        - %s

                        Follow the outline structure. Write each section with practical,
                        beginner-friendly content. Use short sentences and plain language.
                        Include code examples but keep them short and simple.

                        Format the output as proper Markdown:
                        - Use # for the main title and ## for section headings.
                        - Wrap code examples in fenced code blocks (```language).
                        - Use bullet lists, bold, and inline code where appropriate.
                        - Leave a blank line before and after headings and code blocks.
                        """.formatted(outline.title(), sectionsText)
                );
    }

    @AchievesGoal(description = "A reviewed and polished blog post")
    @Action(description = "Review and improve the draft")
    public ReviewedPost reviewDraft(BlogDraft draft, Ai ai) {
        ReviewedPost reviewed = ai
                .withLlmByRole("reviewer")
                .withId("blog-post-reviewer")
                .withPromptContributor(Personas.REVIEWER)
                .creating(ReviewedPost.class)
                .fromPrompt("""
                        You are a technical editor. Review and improve this blog post.

                        Title: %s
                        Content:
                        %s

                        Fix any technical errors. Tighten the writing.
                        Preserve proper Markdown formatting throughout:
                        - Keep # for the title and ## for section headings.
                        - Keep fenced code blocks (```language) intact.
                        - Ensure blank lines before and after headings and code blocks.
                        Provide the revised title, revised content, and a brief
                        summary of the changes you made as feedback.
                        """.formatted(draft.title(), draft.content())
                );

        writeToFile(reviewed);
        return reviewed;
    }

    private void writeToFile(ReviewedPost post) {
        String filename = post.title()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "")
                + ".md";

        Path outputDir = Path.of(properties.outputDir());
        Path filePath = outputDir.resolve(filename);

        try {
            Files.createDirectories(outputDir);
            Files.writeString(filePath, post.content());
            log.info("Blog post written to {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write blog post to {}: {}", filePath, e.getMessage());
        }
    }
}
