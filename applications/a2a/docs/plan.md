# Implementation Plan: A2A Değişiklik Etki Analizi Asistanı

## Context

Onaylı spec: `applications/a2a/docs/spec.md` (kaynak vizyon: `applications/a2a/docs/vision.md`). Amaç: bir ticket geldiğinde local repository'leri analiz edip Markdown etki analizi üreten çok-ajanlı örnek uygulama — üç Spring AI kavramını birleştirAccording to the suggested development plan in @file:plan.md file, create a detailed enumerated technical task list. Link the tasks to the corresponding items in the development plan as well as in the requirements document in @file:proposal.md file. Task items should have a placeholder [ ] for marking as done [x] upon task completion. Write the task list to docs/tasks.md file. Update CLAUDE.md only with the concise technical instructions how to work with the task list.ir: A2A sunucusu (`spring-ai-a2a`), TaskTool ile uzak+yerel alt ajan delegasyonu (`spring-ai-agent-utils`), dosya tabanlı uzun süreli hafıza (`AutoMemoryToolsAdvisor`).

Kullanıcı kararları:

- Koordinatör + 2 repository A2A ajanı + hafıza; test-planner ve security-reviewer **yerel Markdown alt ajanı**.
- Analiz hedefleri workspace'teki gerçek projeler: `applications/todowritetool`, `applications/multi-agent-rag-spring`.
- Tek parametrik `repo-agent/` codebase'i, iki profille iki instance.
- Uzak ajan çağrısı **TaskTool üzerinden** (spec'teki gibi; airbnb-planner'ın `RemoteAgentConnections` deseni yalnızca uyumsuzluk çıkarsa yedek).
- Spec commit edilmedi; commit talimatı gelmeden commit yok.

## Doğrulanmış API'ler (araştırma sonucu)

- **spring-ai-a2a-server-autoconfigure:0.3.0** (org.springaicommunity, BOM yok, sürüm elle): app yalnızca `AgentCard` + `AgentExecutor` bean'i tanımlar; controller/JSON-RPC/`/.well-known/agent-card.json` auto-config. Örnek: repo'daki weather-agent (`new AgentCard.Builder()...`, `new DefaultAgentExecutor(chatClient, (chat, ctx) -> chat.prompt().user(DefaultAgentExecutor.extractTextFromMessage(ctx.getMessage())).call().content())`). Importlar: `io.a2a.spec.*`, `io.a2a.server.agentexecution.*`, `org.springaicommunity.a2a.server.executor.*`. Özellik: `spring.ai.a2a.server.enabled=true`. 0.3.0, Spring AI 2.0.0-M2/Boot 4.0'da doğrulanmış; bizim Boot 4.1 + AI 2.0.0 GA daha yeni → uyumsuzluk derlemede yakalanır.
- **spring-ai-agent-utils 0.10.0** (BOM: `spring-ai-agent-utils-bom:0.10.0`) + `spring-ai-agent-utils-a2a`: `TaskTool.builder().subagentTypes(ClaudeSubagentType.builder().chatClientBuilder("default", b).build()).subagentReferences(ClaudeSubagentReferences.fromRootDirectory(dir)).subagentReferences(new SubagentReference(url, A2ASubagentDefinition.KIND)).subagentTypes(new SubagentType(new A2ASubagentResolver(), new A2ASubagentExecutor())).build()` → `ToolCallback`; `builder.defaultTools(taskTool)` (`defaultToolCallbacks` 0.9+'da deprecated). Hafıza: `AutoMemoryToolsAdvisor.builder().memoriesRootDirectory(dir).build()` → `defaultAdvisors(...)`. Starter/auto-config yok, elle wiring. **DİKKAT:** paket yolları sürümler arasında değişti — importlar jar'dan doğrulanacak (aşağıda Task 5 alt adımı).

## Kararlar

| Konu               | Karar                                                                                                                                                                                                        |
| ------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Paketler           | `org.phoenix.repoagent`, `org.phoenix.coordinator`                                                                                                                                                       |
| Config formatı    | `application.properties` + `application-<profil>.properties` (workspace standardı; spec'in `.yml` ifadesinden bilinçli sapma)                                                                        |
| repo-agent props   | `repo.agent.name/description/root` → `@ConfigurationProperties(prefix="repo.agent") record RepoAgentProperties(String name, String description, String root)`                                           |
| coordinator props  | `coordinator.repo-agent-urls` (liste), `coordinator.agents-dir=agents`, `coordinator.memory-dir=memory` → record `CoordinatorProperties`                                                            |
| Markdown ajan yeri | `applications/a2a/coordinator/agents/` (dosya sistemi dizini, resources DEĞİL — `fromRootDirectory` dosya yolu alır; spec'ten bilinçli sapma, commit edilir)                                        |
| Coordinator wiring | Tek`@Bean ChatClient`; `builder.clone()` default'lar eklenmeden ÖNCE alınıp alt ajanlara verilir (alt ajanların TaskTool+memory miras almaması için). `ChatClientBuilderCustomizer` KULLANILMAZ. |
| Kırpma            | 50_000 byte +`"\n... [TRUNCATED: file larger than 50KB]"`                                                                                                                                                  |
| Hariç dizinler    | `target`, `node_modules`, `.git`                                                                                                                                                                       |
| Dil                | Java sistem prompt'ları ve .md ajanlar İngilizce; rapor çıktısı + README Türkçe                                                                                                                      |

## Görevler

Task 0 sonrası iki bağımsız track paralel ilerleyebilir: **A (repo-agent: 1–4)**, **B (coordinator: 5–6)**. Task 7–8 en son.

### Task 0 — demo/ iskeletini sil

`rm -rf applications/a2a/demo` (git'te untracked). Doğrula: `ls applications/a2a` → yalnızca `docs`.

### Task 1 — repo-agent iskeleti

`applications/a2a/repo-agent/`:

- `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`, `.gitattributes`, `.gitignore`, `.sdkmanrc` → `applications/todowritetool/`'dan kopyala; `chmod +x mvnw`. `.env.example`: `OPENAI_API_KEY=sk-proj-`.
- `pom.xml`: todowritetool pom'unun klonu; artifactId `repo-agent`; deps: `spring-boot-starter-webmvc`, `spring-ai-starter-model-openai` (BOM), `org.springaicommunity:spring-ai-a2a-server-autoconfigure:0.3.0` (sürüm elle), test `spring-boot-starter-webmvc-test`; `spring-ai-bom:2.0.0` import.
- `RepoAgentApplication.java` (`@SpringBootApplication` + `@ConfigurationPropertiesScan`).
- `application.properties`: `spring.application.name=repo-agent`, api-key, `spring.ai.a2a.server.enabled=true`, `logging.level.org.springframework.ai=WARN`.
- `application-todowritetool.properties`: `server.port=8081`, `repo.agent.name=TodoWriteToolRepoAgent`, `repo.agent.root=../../todowritetool`, description = todowritetool repo'sunu tarif eden yönlendirme metni (koordinatörün doğru ajan seçimi buna dayanır).
- `application-rag.properties`: `server.port=8082`, `repo.agent.name=MultiAgentRagRepoAgent`, `repo.agent.root=../../multi-agent-rag-spring`, benzer description.

**Doğrula (1. uyumluluk kapısı):** `./mvnw -q compile` — 0.3.0'ın Boot 4.1/AI 2.0.0 GA ile çözümlendiğini teyit eder; hata çıkarsa tam hatayı kaydet, transitive sürüm çakışmasıysa BOM/exclusion ile düzelt.

### Task 2 — RepoAgentProperties + RepositoryTools (sandbox + 3 tool)

- `config/RepoAgentProperties.java` (record, yukarıdaki gibi).
- `tools/RepositoryTools.java`: constructor `Path repoRoot` (canonical'e çevrilir, yoksa fırlatır) — testlerin `@TempDir` ile doğrudan kurabilmesi için; bean fabrikası `RepoAgentProperties`'ten kurar.
  - `resolveSandboxed(String)`: canonical path repo kökü ile başlamıyorsa `IllegalArgumentException("Path is outside the repository root: ...")`.
  - `@Tool listFiles()`: `Files.walk`, hariç dizin bileşeni içerenleri at, sıralı relative path listesi.
  - `@Tool readFile(path)`: sandbox → en fazla 50_000 byte (`readNBytes`), büyükse marker ekle. `IOException`/`IllegalArgumentException` → mesajı tool sonucu olarak döndür.
  - `@Tool searchInFiles(pattern)`: `Pattern.compile`, `PatternSyntaxException`'da `Pattern.quote` fallback; hariç olmayan dosyalarda `relativePath:lineNo: line`, 200 satır cap + not; okunamayan/binary dosyayı atla.

### Task 3 — repo-agent birim testleri (Task 4 ile paralel)

`tools/RepositoryToolsTest.java` — saf JUnit 5, `@TempDir`:

- Traversal reddi (`../...` ve kök dışı absolute path → "outside the repository root" mesajı).
- Kırpma (60KB dosya → ~50_000 + `[TRUNCATED`).
- Hariç dizinler (`target/`, `node_modules/`, `.git/` listede ve aramada görünmez; `src/Main.java` görünür).
- Arama happy path (`path:lineNo:` formatı).
  Ayrıca `RepoAgentApplicationTests.contextLoads()` — `@SpringBootTest(properties={"spring.ai.openai.api-key=test","repo.agent.name=t","repo.agent.description=t","repo.agent.root=.","server.port=0"})`; A2A auto-config mock ortamda sorun çıkarırsa bu testi sil (spec yalnızca sandbox testini şart koşar).
  **Doğrula:** `./mvnw test` yeşil.

### Task 4 — A2A sunucu wiring

`config/A2aServerConfig.java`: `AgentCard` bean'i (`props.name/description`, `url("http://localhost:"+port+"/")`, `capabilities(streaming(false))`, tek `AgentSkill` id `repository-analysis`, `protocolVersion("0.3.0")`) + `AgentExecutor` bean'i (weather-agent örneğindeki birebir desen; `builder.clone().defaultSystem(SYSTEM_PROMPT.formatted(props.name())).defaultTools(tools).build()`).
Sistem prompt'u (İngilizce): repository analiz uzmanı; yalnızca üç tool; her bulguda dosya yolu/sembol; bulunamayanı uydurmak yok, "bulamadım" de; önce listFiles/searchInFiles ile yönünü bul; kısa yapılandırılmış Markdown.
**Doğrula (2. uyumluluk kapısı):** `./mvnw -q compile` — `DefaultAgentExecutor` GA API'ye uymuyorsa fallback: `AgentExecutor`'ı elle implement et (aynı bean'ler kalır). Smoke: `OPENAI_API_KEY=dummy ./mvnw spring-boot:run -Dspring-boot.run.profiles=todowritetool` → `curl :8081/.well-known/agent-card.json` doğru name/description döner (kart LLM çağrısı gerektirmez).

### Task 5 — coordinator iskeleti

`applications/a2a/coordinator/`: Task 1'deki kopya dosyalar + `.env.example`; `.gitignore`'a ek blok: `memory/`.

- `pom.xml`: artifactId `coordinator`; deps: `spring-boot-starter-webmvc`, `spring-ai-starter-model-openai`, `org.springaicommunity:spring-ai-agent-utils`, `org.springaicommunity:spring-ai-agent-utils-a2a` (ikisi BOM'dan), test `spring-boot-starter-webmvc-test`; dependencyManagement: `spring-ai-bom:2.0.0` + `spring-ai-agent-utils-bom:0.10.0` import.
- `CoordinatorApplication.java`, `application.properties` (port 8080, api-key, `coordinator.repo-agent-urls=http://localhost:8081,http://localhost:8082`, `coordinator.agents-dir=agents`, `coordinator.memory-dir=memory`).

**Doğrula + import çözümleme alt adımı:** `./mvnw -q compile`, ardından gerçek FQCN'leri jar'dan çıkar (varsayma!):
`unzip -l ~/.m2/repository/org/springaicommunity/spring-ai-agent-utils/0.10.0/*.jar | grep -Ei 'TaskTool|Subagent|AutoMemory'` (aynısı `-a2a` jar'ı için). Hedef sınıflar: `TaskTool`, `ClaudeSubagentType`, `ClaudeSubagentReferences`, `SubagentReference`, `SubagentType`, `A2ASubagentDefinition`, `A2ASubagentResolver`, `A2ASubagentExecutor`, `AutoMemoryToolsAdvisor`.

### Task 6 — coordinator implementasyonu

- `config/CoordinatorProperties.java` (record).
- `agents/test-planner.md`: front-matter `name/description` (description: "repo kanıtı toplandıktan SONRA çağır, kanıtı prompt'ta ver") + gövde: risk bazlı unit/integration/contract/regresyon planı; her madde verilen kanıttaki dosya/sınıfa referans; kanıt eksikse boşluğu söyle.
- `agents/security-reviewer.md`: description: "YALNIZCA kimlik/yetki/kişisel veri/dış girdi etkileniyorsa çağır" + gövde: yetki açıkları, veri sızıntısı, saldırı yüzeyi, loglarda hassas veri; severity (high/medium/low); kanıtsız alan için eksik olanı söyle.
- `config/ChatClientConfig.java`: tek `@Bean ChatClient` — önce `subagentBuilder = builder.clone()`; TaskTool'u yukarıdaki doğrulanmış builder zinciriyle kur (Claude tipi + `fromRootDirectory(props.agentsDir())` + URL başına `SubagentReference(url, A2ASubagentDefinition.KIND)` + A2A tipi); sonra `builder.defaultSystem(SYSTEM_PROMPT).defaultTools(taskTool).defaultAdvisors(AutoMemoryToolsAdvisor.builder().memoriesRootDirectory(props.memoryDir()).build()).build()`.
- `SYSTEM_PROMPT` (İngilizce, rapor Türkçe): spec'teki 8 orkestrasyon kuralı (önce kanıt; yalnız ilgili repo ajanı; security koşullu; test-planner her zaman ve kanıttan sonra; ulaşılamayan ajan → "X ajanına ulaşılamadı", uydurma yok; çelişki → "Açık kararlar"; her iddiada dosya/sembol; hafızaya yalnız kalıcı mimari kurallar) + sabit Türkçe şablon: `## İstek / ## Etkilenen bileşenler / ## Önerilen akış / ## Riskler / ## Test planı / ## Açık kararlar`; kod değiştirme yasağı.
- `analyze/AnalyzeService.java` (`chatClient.prompt().user(ticket).call().content()`), `analyze/AnalyzeController.java` (`@PostMapping(value="/analyze", consumes=TEXT_PLAIN_VALUE, produces="text/markdown")`; boş gövde → 400 `ResponseStatusException`).
- `CoordinatorApplicationTests.contextLoads()` — `A2ASubagentResolver` kartları TaskTool kurulumunda eager çekiyorsa (portlar kapalıyken patlar) testi sil, stub makinesi ekleme.
  **Doğrula:** compile + test; `agents/` altındaki iki .md `git status`'ta yeni dosya olarak görünmeli (commit edilecekler; `memory/` görünmemeli).

### Task 7 — Dokümantasyon (A+B derlendikten sonra)

- `applications/a2a/README.md` (Türkçe, workspace yapısı: başlık → intro → ASCII akış diyagramı → Kullanılan teknolojiler → Nasıl çalışır? → Kurulum ve çalıştırma → Kullanım → Proje yapısı → Testler → Kapsam dışı). 3 terminal: T1 `cd applications/a2a/repo-agent && ./mvnw spring-boot:run -Dspring-boot.run.profiles=todowritetool`, T2 aynısı `rag`, T3 `cd applications/a2a/coordinator && ./mvnw spring-boot:run`. **Not:** repo-agent kendi dizininden çalıştırılmalı (relative `repo.agent.root`). Kullanım: agent-card curl'leri + `curl -s -X POST http://localhost:8080/analyze -H 'Content-Type: text/plain' --data "todowritetool'daki todo öğelerine priority alanı ekleyelim — etkilenen sınıfları, riskleri ve test senaryolarını çıkar."`. Hafıza demosu: kural öğret → `coordinator/memory/` dosyası oluşur → yeni analizde kural yansır. Kapsam dışı: spec listesi.
- Kök `README.md` tablosuna satır: `| [a2a](applications/a2a/README.md) | A2A + TaskTool + kalıcı hafıza ile çok-ajanlı değişiklik etki analizi: ... |`

### Task 8 — Manuel uçtan uca doğrulama (gerçek OPENAI_API_KEY)

1. Kart kontrolleri: 8081 → `TodoWriteToolRepoAgent`, 8082 → `MultiAgentRagRepoAgent`.
2. Örnek ticket POST → 6 şablon bölümü tam; atıf yapılan path'ler gerçekten var (spot-check); T1 loglarında tool aktivitesi, T2 sessiz; test-planner kanıttan sonra; security-reviewer bu ticket'ta ÇAĞRILMAMIŞ.
3. Negatif yönlendirme: rag temalı ticket ("multi-agent-rag-spring'e yeni web arama sağlayıcısı ekleyelim") → yalnız 8082 aktif.
4. Dürüstlük: T2'yi durdur, rag ticket'ı tekrar → raporda "ulaşılamadı", uydurma bulgu yok.
5. Hafıza: backward-compatibility kuralını öğret → `memory/` altında dosya; `git status`'ta görünmüyor; yeni API ticket'ında kural rapora yansıyor; (opsiyonel) restart sonrası kalıcı.
6. Boş gövde → 400.

## Riskler

- İki community kütüphanesi de uçta: `spring-ai-a2a` 0.3.0 (M2'de doğrulanmış) ve `agent-utils` 0.10.0. İkisi de derleme kapılarında (Task 1/4 ve 5/6) yakalanır. A2A executor için fallback elle `AgentExecutor`; agent-utils için ucuz fallback yok → uyumsuzluğu hemen raporla. Uzak çağrı tarafı tümden tıkanırsa yedek desen: airbnb-planner örneğindeki `RemoteAgentConnections` tarzı elle `@Tool` sınıfı (kullanıcı bilgilendirilerek).
- agent-utils importlarını jar'dan çözmeden yazma (Task 5 alt adımı).
- `@RequestBody String` + text/plain 415 verirse `StringHttpMessageConverter` — ancak e2e'de görülürse dokun.

## Kritik dosyalar

- `applications/a2a/docs/spec.md` — onaylı spec (kaynak gerçek)
- `applications/todowritetool/pom.xml` — pom şablonu
- `applications/todowritetool/src/main/java/org/phoenix/todowritetool/config/ChatClientConfig.java` — wiring deseni (koordinatörde bilinçli sapma: customizer yok)
- `applications/todowritetool/src/main/java/org/phoenix/todowritetool/agent/AgentController.java` — controller/400 deseni
- `README.md` (kök) — tabloya satır eklenecek

## Commit politikası

Kullanıcı spec için "commit etme" dedi; bu iş boyunca kullanıcı istemeden commit yapılmayacak.
