# Spring AI ile Agent Skills (SkillsJar)

Spring AI üzerinde **SkillsJar** konusunda bir proje yapacağım.
Projeyi oluşturdum, onun üzerinden ilerleyeceğim.

## Kullanılacak SkillsJar

```
"com.skillsjars" % "anthropics__skills__pdf" % "2026_02_25-3d59511"
```

Birlikte kullanılacak araçlar:

- `ShellTools`
- `FileSystemTools`

> Bu sayfada SkillsJar için kurulumları anlatıyor.

## Senaryo

Kullanıcı bir konu verir (örn. _"su samuru"_). Agent o konuda bir hikâye yazar (LLM) ve
PDF skill'ini (JAR'dan yüklenen) `ShellTools` + `FileSystemTools` ile çalıştırarak hikâyeyi
`/path/to/otter-story.pdf` dosyasına yazar.

**Tetikleyici:** REST endpoint.

---

# SkillsJars

- Find SkillsJars
- Documentation
- Agent Skills on Maven Central

## Using SkillsJars

SkillsJars are Agent Skills packaged as JARs on Maven Central. They can be used with AI code
assistants, custom agents, and frameworks like Spring AI. Managing Agent Skills as packaged
dependencies enables versioning, grouping as transitive dependencies, and avoiding copy &
pasting files.

> **Security Warning**
>
> Agent Skills can be dangerous / do malicious things and should be vetted before use. While
> SkillsJars does a basic security scan of the Skills before they are published, it is not a
> substitute for proper security reviews.

## AI Code Assistants

Most AI code assistants expect Agent Skills as files on the filesystem. The SkillsJars build
plugins extract skills from your project dependencies into a directory your assistant can read.

### 1. Add the extraction plugin

**Gradle:**

```kotlin
plugins {
    id("com.skillsjars.gradle-plugin") version "0.0.2"
}
```

**Maven:**

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.skillsjars</groupId>
            <artifactId>maven-plugin</artifactId>
            <version>0.0.6</version>
            <dependencies>
                <!-- Your SkillsJars -->
                <dependency>
                    <groupId>com.skillsjars</groupId>
                    <artifactId>SKILLJAR_ARTIFACT_ID</artifactId>
                    <version>SKILLJAR_VERSION</version>
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</build>
```

### 2. Add SkillsJar dependencies

Browse Agent Skills on SkillsJars.com and add them to your project using the dependency
snippet for your build tool.

### 3. Extract skills

Run the extraction command, specifying the directory your AI assistant expects:

```bash
# Gradle
./gradlew extractSkillsJars -Pdir=.kiro/skills

# Maven
./mvnw skillsjars:extract -Ddir=.kiro/skills
```

Replace `.kiro/skills` with the path your AI assistant uses for skills.

> **Tip: AGENTS.md**
>
> Your project's `AGENTS.md` can instruct AI agents to run the extraction command before
> working with the project. This way, skills are always available without manual setup.

## Custom Agents

### Spring AI

The Spring AI Agent Utils project provides a `SkillsTool` that integrates Agent Skills directly
with Spring AI agents. SkillsJars work out of the box — skills are read directly from the
classpath with no extraction step needed.

#### 1. Add dependencies

Add the Spring AI Agent Utils library and any SkillsJar dependencies to your project:

```xml
<dependency>
    <groupId>org.springaicommunity</groupId>
    <artifactId>spring-ai-agent-utils</artifactId>
    <version>0.5.0</version>
</dependency>

<!-- SkillsJar dependencies, for example -->
<dependency>
    <groupId>com.skillsjars</groupId>
    <artifactId>browser-use__browser-use__browser-use</artifactId>
    <version>2026_02_23-1d154e1</version>
</dependency>
```

#### 2. Configure the skills path

In `application.properties`, point to the classpath location where SkillsJars store their skills:

```properties
agent.skills.paths=classpath:/META-INF/skills
```

#### 3. Wire up the SkillsTool

Use the `SkillsTool` builder to load skills and add them to your `ChatClient`:

```java
@Value("${agent.skills.paths}")
List<Resource> skillPaths;

ChatClient chatClient = chatClientBuilder
        .defaultToolCallbacks(
                SkillsTool.builder().addSkillsResources(skillPaths).build()
        )
        .build();
```

> **Example project**
>
> See the `skillsjars-example-spring-ai` repository for a complete working example.

## Reading SkillsJars Directly

Custom agents on the JVM can read skills directly from SkillsJar dependencies on the classpath.
Skills are located at a well-known path inside the JAR:

```
META-INF/skills/<org>/<repo>/<skill>/SKILL.md
```

Each `SKILL.md` follows the Agent Skills specification with YAML front-matter containing the
skill name, description, and other metadata. Additional files referenced by the skill are
included alongside the `SKILL.md`.