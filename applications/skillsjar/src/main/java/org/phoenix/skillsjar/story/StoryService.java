package org.phoenix.skillsjar.story;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Locale;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StoryService {

    private final ChatClient chatClient;
    private final String outputDir;

    public StoryService(ChatClient chatClient, @Value("${app.output-dir}") String outputDir) {
        this.chatClient = chatClient;
        this.outputDir = outputDir;
    }

    public StoryResult generate(String topic) {
        Path pdfPath = resolvePdfPath(topic);

        String summary = chatClient.prompt()
                .user("""
                        Write an original short story about the following topic: "%s".
                        Then create a PDF file containing the story at exactly this absolute path:
                        %s
                        Use the pdf skill to produce the PDF.
                        """.formatted(topic, pdfPath))
                .call()
                .content();

        return new StoryResult(pdfPath.toString(), summary);
    }

    private Path resolvePdfPath(String topic) {
        try {
            Path dir = Path.of(outputDir).toAbsolutePath();
            Files.createDirectories(dir);
            return dir.resolve(slugify(topic) + ".pdf");
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to prepare output directory: " + outputDir, e);
        }
    }

    static String slugify(String topic) {
        String normalized = Normalizer.normalize(topic, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+)|(-+$)", "");
        return StringUtils.hasText(normalized) ? normalized : "story";
    }

    public record StoryResult(String pdfPath, String summary) {
    }
}
