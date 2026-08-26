package org.phoenix.repoagent.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryToolsTest {

    @TempDir
    Path tempDir;

    private Path repoRoot;
    private RepositoryTools tools;

    @BeforeEach
    void createRepositoryWithSampleFiles() throws IOException {
        repoRoot = Files.createDirectory(tempDir.resolve("repo"));
        writeRepoFile("src/Main.java", "class Main {\n    void run() {}\n}\n");
        writeRepoFile("target/Generated.txt", "class Main in build output\n");
        writeRepoFile("node_modules/lib.js", "class Main in dependency\n");
        writeRepoFile(".git/config", "class Main in vcs metadata\n");
        tools = new RepositoryTools(repoRoot);
    }

    @Test
    void whenConstructedWithMissingRoot_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new RepositoryTools(tempDir.resolve("missing")));
    }

    @Test
    void whenReadingPathWithParentTraversal_reportsOutsideRepositoryRoot() throws IOException {
        Files.writeString(tempDir.resolve("secret.txt"), "top secret");

        String result = tools.readFile("../secret.txt");

        assertTrue(result.contains("outside the repository root"), result);
        assertFalse(result.contains("top secret"), result);
    }

    @Test
    void whenReadingAbsolutePathOutsideRoot_reportsOutsideRepositoryRoot() throws IOException {
        Path outside = Files.writeString(tempDir.resolve("outside.txt"), "top secret");

        String result = tools.readFile(outside.toString());

        assertTrue(result.contains("outside the repository root"), result);
        assertFalse(result.contains("top secret"), result);
    }

    @Test
    void whenReading60KbFile_truncatesAt50KbWithMarker() throws IOException {
        writeRepoFile("big.txt", "x".repeat(60_000));

        String result = tools.readFile("big.txt");

        assertEquals(50_000, result.indexOf("\n... [TRUNCATED"), "content before marker should be 50000 bytes");
        assertTrue(result.contains("[TRUNCATED: file larger than 50KB]"), result.substring(49_990));
    }

    @Test
    void whenReadingSmallFile_returnsFullContentWithoutMarker() {
        String result = tools.readFile("src/Main.java");

        assertEquals("class Main {\n    void run() {}\n}\n", result);
    }

    @Test
    void whenReadingMissingFile_returnsErrorMessageInsteadOfThrowing() {
        String result = tools.readFile("does-not-exist.txt");

        assertFalse(result.isBlank());
    }

    @Test
    void listFiles_excludesBuildAndVcsDirectories() {
        String listing = tools.listFiles();

        assertTrue(listing.contains("src/Main.java"), listing);
        assertFalse(listing.contains("target"), listing);
        assertFalse(listing.contains("node_modules"), listing);
        assertFalse(listing.contains(".git"), listing);
    }

    @Test
    void whenSearchingExistingText_returnsPathLineNumberAndLine() {
        String result = tools.searchInFiles("void run");

        assertEquals("src/Main.java:2:     void run() {}", result);
    }

    @Test
    void searchInFiles_excludesBuildAndVcsDirectories() {
        String result = tools.searchInFiles("class Main");

        assertTrue(result.contains("src/Main.java:1: class Main {"), result);
        assertFalse(result.contains("target"), result);
        assertFalse(result.contains("node_modules"), result);
        assertFalse(result.contains(".git"), result);
    }

    @Test
    void whenSearchPatternIsInvalidRegex_fallsBackToLiteralSearch() throws IOException {
        writeRepoFile("notes.txt", "price is $10 (approx\n");

        String result = tools.searchInFiles("$10 (approx");

        assertTrue(result.contains("notes.txt:1:"), result);
    }

    @Test
    void whenMoreThan200LinesMatch_capsResultsAndSaysSo() throws IOException {
        writeRepoFile("many.txt", "matching line\n".repeat(250));

        String result = tools.searchInFiles("matching line");

        assertEquals(200, result.lines().filter(line -> line.contains("many.txt:")).count());
        assertTrue(result.contains("capped"), result);
    }

    private void writeRepoFile(String relativePath, String content) throws IOException {
        Path file = repoRoot.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }

}
