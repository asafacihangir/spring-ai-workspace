package org.phoenix.repoagent.tools;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * Read-only file tools sandboxed to a single repository root. Every path coming
 * from the LLM is canonicalized and rejected if it escapes the root.
 */
public class RepositoryTools {

    private static final Set<String> EXCLUDED_DIRECTORIES = Set.of("target", "node_modules", ".git");
    private static final int MAX_FILE_BYTES = 50_000;
    private static final int MAX_SEARCH_RESULTS = 200;
    private static final String TRUNCATION_MARKER = "\n... [TRUNCATED: file larger than 50KB]";
    private static final String SEARCH_CAP_MARKER = "\n... [Result capped at " + MAX_SEARCH_RESULTS + " matching lines]";

    private final Path repoRoot;

    public RepositoryTools(Path repoRoot) {
        try {
            this.repoRoot = repoRoot.toRealPath();
        } catch (IOException e) {
            throw new IllegalArgumentException("Repository root does not exist: " + repoRoot, e);
        }
    }

    @Tool(description = "List every file in the repository as a sorted list of relative paths. Build and VCS directories (target, node_modules, .git) are excluded.")
    public String listFiles() {
        try (Stream<Path> paths = Files.walk(repoRoot)) {
            List<String> files = paths.filter(Files::isRegularFile)
                    .map(repoRoot::relativize)
                    .filter(path -> !isExcluded(path))
                    .map(Path::toString)
                    .sorted()
                    .toList();
            return String.join("\n", files);
        } catch (IOException e) {
            return "Error listing files: " + e.getMessage();
        }
    }

    @Tool(description = "Read a file by its path relative to the repository root. Files larger than 50KB are truncated and marked as such.")
    public String readFile(@ToolParam(description = "Path relative to the repository root, e.g. src/main/java/App.java") String path) {
        try {
            return readTruncated(resolveSandboxed(path));
        } catch (IOException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    @Tool(description = "Search all repository files for a regular expression (falls back to literal text if the regex is invalid). Returns matches as 'relativePath:lineNumber: line', capped at 200 lines.")
    public String searchInFiles(@ToolParam(description = "Regular expression or literal text to search for") String pattern) {
        Pattern regex = compileLenient(pattern);
        List<String> matches = new ArrayList<>();
        for (Path file : includedFiles()) {
            collectMatches(file, regex, matches);
        }
        if (matches.isEmpty()) {
            return "No matches found for pattern: " + pattern;
        }
        String result = String.join("\n", matches);
        return matches.size() < MAX_SEARCH_RESULTS ? result : result + SEARCH_CAP_MARKER;
    }

    private Path resolveSandboxed(String path) throws IOException {
        Path resolved = repoRoot.resolve(path).normalize();
        if (!resolved.startsWith(repoRoot)) {
            throw new IllegalArgumentException("Path is outside the repository root: " + path);
        }
        if (Files.exists(resolved) && !resolved.toRealPath().startsWith(repoRoot)) {
            throw new IllegalArgumentException("Path is outside the repository root: " + path);
        }
        return resolved;
    }

    private String readTruncated(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            String content = new String(in.readNBytes(MAX_FILE_BYTES), StandardCharsets.UTF_8);
            return in.read() == -1 ? content : content + TRUNCATION_MARKER;
        }
    }

    private List<Path> includedFiles() {
        try (Stream<Path> paths = Files.walk(repoRoot)) {
            return paths.filter(Files::isRegularFile)
                    .filter(file -> !isExcluded(repoRoot.relativize(file)))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private void collectMatches(Path file, Pattern regex, List<String> matches) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            return; // unreadable or binary file: skip
        }
        String relativePath = repoRoot.relativize(file).toString();
        for (int i = 0; i < lines.size() && matches.size() < MAX_SEARCH_RESULTS; i++) {
            if (regex.matcher(lines.get(i)).find()) {
                matches.add(relativePath + ":" + (i + 1) + ": " + lines.get(i));
            }
        }
    }

    private boolean isExcluded(Path relativePath) {
        for (Path component : relativePath) {
            if (EXCLUDED_DIRECTORIES.contains(component.toString())) {
                return true;
            }
        }
        return false;
    }

    private Pattern compileLenient(String pattern) {
        try {
            return Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            return Pattern.compile(Pattern.quote(pattern));
        }
    }

}
