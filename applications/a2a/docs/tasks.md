# Görev Listesi: A2A Değişiklik Etki Analizi Asistanı

> Kaynaklar: [plan.md](plan.md) (uygulama planı) ve [spec.md](spec.md) (onaylı tasarım).
> Tamamlanan görev `[x]` ile işaretlenir. Task 0 sonrası iki bağımsız track paralel ilerleyebilir:
> **Track A (repo-agent: Görev 1–4)**, **Track B (coordinator: Görev 5–6)**; Görev 7–8 en son.

## Görev 0 — demo/ iskeletini sil

Plan: [Task 0](plan.md#task-0--demo-iskeletini-sil) · Spec: [Kapsam Kararları](spec.md#kapsam-kararları) ("Mevcut `demo/` iskeleti silinir")

- [x] 0.1 `rm -rf applications/a2a/demo` (git'te untracked olduğu doğrulanarak)
- [x] 0.2 Doğrula: `ls applications/a2a` çıktısı yalnızca `docs` içeriyor *(not: dizinde ayrıca gitignore'lu `spring-ai-a2a/` araştırma clone'u duruyor; plana dahil değil, dokunulmadı)*

## Görev 1 — repo-agent iskeleti (Track A)

Plan: [Task 1](plan.md#task-1--repo-agent-iskeleti) · Spec: [Yerleşim](spec.md#yerleşim), [repo-agent (A2A sunucusu)](spec.md#repo-agent-a2a-sunucusu), [Bağımlılıklar](spec.md#bağımlılıklar)

- [x] 1.1 `applications/a2a/repo-agent/` altına `applications/todowritetool/`'dan kopyala: `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`, `.gitattributes`, `.gitignore`, `.sdkmanrc`; `chmod +x mvnw`
- [x] 1.2 `.env.example` oluştur (`OPENAI_API_KEY=sk-proj-`)
- [x] 1.3 `pom.xml`: todowritetool pom klonu; artifactId `repo-agent`; bağımlılıklar `spring-boot-starter-webmvc`, `spring-ai-starter-model-openai` (BOM), `org.springaicommunity:spring-ai-a2a-server-autoconfigure:0.3.0` (sürüm elle), test `spring-boot-starter-webmvc-test`; `spring-ai-bom:2.0.0` import
- [x] 1.4 `RepoAgentApplication.java` (`@SpringBootApplication` + `@ConfigurationPropertiesScan`)
- [x] 1.5 `application.properties`: `spring.application.name=repo-agent`, api-key, `spring.ai.a2a.server.enabled=true`, `logging.level.org.springframework.ai=WARN` *(+e2e'de eklendi: `a2a.blocking.agent.timeout.seconds=300`, bkz. Uygulama notları #3)*
- [x] 1.6 `application-todowritetool.properties`: `server.port=8081`, `repo.agent.name=TodoWriteToolRepoAgent`, `repo.agent.root=../../todowritetool`, yönlendirici description
- [x] 1.7 `application-rag.properties`: `server.port=8082`, `repo.agent.name=MultiAgentRagRepoAgent`, `repo.agent.root=../../multi-agent-rag-spring`, benzer description
- [x] 1.8 **1. uyumluluk kapısı:** `./mvnw -q compile` yeşil — 0.3.0, Boot 4.1 / AI 2.0.0 GA ile sorunsuz çözümlendi (tek pürüz ortamın varsayılan JDK'sıydı; `JAVA_HOME=~/.sdkman/candidates/java/21.0.2-tem` ile çalıştırıldı)

## Görev 2 — RepoAgentProperties + RepositoryTools (Track A)

Plan: [Task 2](plan.md#task-2--repoagentproperties--repositorytools-sandbox--3-tool) · Spec: [repo-agent (A2A sunucusu)](spec.md#repo-agent-a2a-sunucusu) (üç `@Tool` + trust boundary), [Hata Yönetimi](spec.md#hata-yönetimi)

- [x] 2.1 `config/RepoAgentProperties.java`: `@ConfigurationProperties(prefix="repo.agent") record RepoAgentProperties(String name, String description, String root)`
- [x] 2.2 `tools/RepositoryTools.java` constructor'ı `Path repoRoot` alır (canonical'e çevrilir, yoksa fırlatır); bean fabrikası `A2aServerConfig` içinde `RepoAgentProperties`'ten kurar
- [x] 2.3 `resolveSandboxed(String)`: canonical path repo kökü ile başlamıyorsa `IllegalArgumentException("Path is outside the repository root: ...")` (+symlink kaçışına karşı `toRealPath` kontrolü)
- [x] 2.4 `@Tool listFiles()`: `Files.walk`; `target`, `node_modules`, `.git` bileşeni içerenler hariç; sıralı relative path listesi
- [x] 2.5 `@Tool readFile(path)`: sandbox → en fazla 50_000 byte (`readNBytes`); büyükse `"\n... [TRUNCATED: file larger than 50KB]"` marker; `IOException`/`IllegalArgumentException` mesajı tool sonucu olarak döner
- [x] 2.6 `@Tool searchInFiles(pattern)`: `Pattern.compile`, `PatternSyntaxException`'da `Pattern.quote` fallback; `relativePath:lineNo: line` formatı; 200 satır cap + not; okunamayan/binary dosya atlanır

## Görev 3 — repo-agent birim testleri (Track A, Görev 4 ile paralel)

Plan: [Task 3](plan.md#task-3--repo-agent-birim-testleri-task-4-ile-paralel) · Spec: [Test ve Doğrulama](spec.md#test-ve-doğrulama) (yalnız deterministik sandbox davranışı birim testlenir)

- [x] 3.1 `tools/RepositoryToolsTest.java` (saf JUnit 5, `@TempDir`): traversal reddi — `../...` ve kök dışı absolute path → "outside the repository root" mesajı *(TDD: testler implementasyondan ÖNCE yazıldı, RED derleme hatasıyla doğrulandı)*
- [x] 3.2 Kırpma testi: 60KB dosya → 50_000 byte + `[TRUNCATED` marker
- [x] 3.3 Hariç dizin testi: `target/`, `node_modules/`, `.git/` liste ve aramada görünmez; `src/Main.java` görünür
- [x] 3.4 Arama happy path testi: `path:lineNo:` formatı (+geçersiz regex fallback ve 200 satır cap testleri)
- [x] 3.5 `RepoAgentApplicationTests.contextLoads()` — A2A auto-config mock ortamda sorun çıkarmadı, test tutuldu
- [x] 3.6 Doğrula: `./mvnw test` yeşil — 12/12 test geçti

## Görev 4 — A2A sunucu wiring (Track A)

Plan: [Task 4](plan.md#task-4--a2a-sunucu-wiring) · Spec: [repo-agent (A2A sunucusu)](spec.md#repo-agent-a2a-sunucusu) (`AgentCard` + `AgentExecutor` bean'leri, sistem prompt özü), [Hata Yönetimi](spec.md#hata-yönetimi)

- [x] 4.1 `config/A2aServerConfig.java` — `AgentCard` bean'i: `props.name/description`, `url("http://localhost:"+port+"/")`, `capabilities(streaming(false))`, tek `AgentSkill` id `repository-analysis`, `protocolVersion("0.3.0")`
- [x] 4.2 `AgentExecutor` bean'i (weather-agent deseni): `builder.clone().defaultSystem(SYSTEM_PROMPT.formatted(props.name())).defaultTools(tools).build()`
- [x] 4.3 İngilizce sistem prompt'u: repository analiz uzmanı; yalnızca üç tool; her bulguda dosya yolu/sembol; uydurma yok; önce listFiles/searchInFiles; kısa yapılandırılmış Markdown
- [x] 4.4 **2. uyumluluk kapısı:** `./mvnw -q compile` yeşil — `DefaultAgentExecutor` GA API ile uyumlu, elle fallback gerekmedi
- [x] 4.5 Smoke: dummy key ile başlatıldı → `curl :8081/.well-known/agent-card.json` doğru name/description döndü

## Görev 5 — coordinator iskeleti (Track B)

Plan: [Task 5](plan.md#task-5--coordinator-iskeleti) · Spec: [coordinator (EngineeringCoordinatorAgent)](spec.md#coordinator-engineeringcoordinatoragent), [Bağımlılıklar](spec.md#bağımlılıklar)

- [x] 5.1 `applications/a2a/coordinator/`: Görev 1.1'deki kopya dosyalar + `.env.example`; `.gitignore`'a ek blok: `memory/`
- [x] 5.2 `pom.xml`: artifactId `coordinator`; bağımlılıklar `spring-boot-starter-webmvc`, `spring-ai-starter-model-openai`, `org.springaicommunity:spring-ai-agent-utils`, `org.springaicommunity:spring-ai-agent-utils-a2a` (ikisi BOM'dan), test `spring-boot-starter-webmvc-test`; dependencyManagement: `spring-ai-bom:2.0.0` + `spring-ai-agent-utils-bom:0.10.0`
- [x] 5.3 `CoordinatorApplication.java` + `application.properties` (port 8080, api-key, `coordinator.repo-agent-urls`, `coordinator.agents-dir=agents`, `coordinator.memory-dir=memory`)
- [x] 5.4 Doğrula: `./mvnw -q compile` yeşil; FQCN'ler jar'lardan çözüldü — hedef sınıfların tümü mevcut; generic `SubagentReference`/`SubagentType` transitive `spring-ai-agent-utils-common` jar'ında, paket `org.springaicommunity.agent.common.task.subagent` (bkz. Uygulama notları #1)

## Görev 6 — coordinator implementasyonu (Track B)

Plan: [Task 6](plan.md#task-6--coordinator-implementasyonu) · Spec: [coordinator (EngineeringCoordinatorAgent)](spec.md#coordinator-engineeringcoordinatoragent) (TaskTool 4 alt ajan + hafıza + orkestrasyon kuralları), [Veri Akışı](spec.md#veri-akışı), [Hata Yönetimi](spec.md#hata-yönetimi)

- [x] 6.1 `config/CoordinatorProperties.java`: record (`repoAgentUrls` listesi, `agentsDir`, `memoryDir`)
- [x] 6.2 `agents/test-planner.md`: front-matter `name/description` (kanıt-sonrası çağrı talimatı) + risk bazlı unit/integration/contract/regresyon plan gövdesi
- [x] 6.3 `agents/security-reviewer.md`: koşullu çağrı description'ı + yetki/veri sızıntısı/saldırı yüzeyi/log incelemesi, severity, eksik kanıt bildirimi
- [x] 6.4 `config/ChatClientConfig.java`: tek `@Bean ChatClient` — `subagentBuilder = builder.clone()` default'lardan ÖNCE; TaskTool builder zinciri (Claude tipi + `fromRootDirectory` + URL başına `SubagentReference(url, A2ASubagentDefinition.KIND)` + A2A tipi); `defaultSystem + defaultTools(taskTool) + defaultAdvisors(AutoMemoryToolsAdvisor)` *(A2A tipi executor'ı stok değil, özel `BlockingA2aSubagentExecutor` — bkz. Uygulama notları #2)*
- [x] 6.5 `SYSTEM_PROMPT` (İngilizce, rapor Türkçe): 8 orkestrasyon kuralı + sabit Türkçe şablon (`## İstek / ## Etkilenen bileşenler / ## Önerilen akış / ## Riskler / ## Test planı / ## Açık kararlar`); kod değiştirme yasağı
- [x] 6.6 `analyze/AnalyzeService.java` + `analyze/AnalyzeController.java` (`consumes=TEXT_PLAIN_VALUE`, `produces="text/markdown"`; boş gövde → 400 `ResponseStatusException`)
- [x] 6.7 `CoordinatorApplicationTests.contextLoads()` — plan'daki koşul gerçekleşti: `A2ASubagentResolver` kartları TaskTool kurulumunda eager çekiyor (portlar kapalıyken `A2AClientError: Failed to obtain agent card`); test plan talimatı gereği SİLİNDİ, stub makinesi eklenmedi
- [x] 6.8 Doğrula: compile + test yeşil (6/6: `AnalyzeControllerTest` 2 + `BlockingA2aSubagentExecutorTest` 4); `agents/` altındaki iki .md `git status`'ta görünüyor, `memory/` görünmüyor (gitignore doğrulandı)

## Görev 7 — Dokümantasyon (Track A+B derlendikten sonra)

Plan: [Task 7](plan.md#task-7--dokümantasyon-ab-derlendikten-sonra) · Spec: [Test ve Doğrulama](spec.md#test-ve-doğrulama) (e2e ve hafıza demosu README'de), [Kapsam Dışı](spec.md#kapsam-dışı-ilk-sürüm)

- [x] 7.1 `applications/a2a/README.md` (Türkçe, workspace yapısı: başlık → intro → ASCII akış diyagramı → Kullanılan teknolojiler → Nasıl çalışır? → Kurulum ve çalıştırma → Kullanım → Proje yapısı → Testler → Kapsam dışı)
- [x] 7.2 README'de 3 terminal talimatı + başlatma sırası notu (koordinatör açılışta agent card çektiği için repo ajanları önce başlatılmalı) + "repo-agent kendi dizininden çalıştırılmalı" notu
- [x] 7.3 README kullanım bölümü: agent-card curl'leri + örnek ticket curl'ü
- [x] 7.4 README hafıza demosu + Kapsam dışı bölümü (spec listesi)
- [x] 7.5 Kök `README.md` tablosuna `a2a` satırı eklendi

## Görev 8 — Manuel uçtan uca doğrulama (gerçek OPENAI_API_KEY)

Plan: [Task 8](plan.md#task-8--manuel-uçtan-uca-doğrulama-gerçek-openai_api_key) · Spec: [Test ve Doğrulama](spec.md#test-ve-doğrulama), [Başarı Kriterleri](spec.md#başarı-kriterleri-vizyondan-bu-kapsama-uyarlanmış), [Hata Yönetimi](spec.md#hata-yönetimi)

- [x] 8.1 Kart kontrolleri: 8081 → `TodoWriteToolRepoAgent`, 8082 → `MultiAgentRagRepoAgent` ✅
- [x] 8.2 Örnek ticket POST → 6 şablon bölümü tam; atıf yapılan path'ler gerçekten var (4 dosya spot-check ✅); yalnız 8081'e delegasyon (10.305 karakter kanıt), 8082 sessiz; security-reviewer ÇAĞRILMADI (raporda gerekçesiyle belirtiliyor); test-planner çağrısı kütüphane loglamadığı için dolaylı doğrulandı (rapor kanıt-referanslı ayrıntılı test planı içeriyor)
- [x] 8.3 Negatif yönlendirme: rag temalı ticket → yalnız 8082'ye delegasyon (7.718 karakter kanıt), 8081 çağrılmadı ✅
- [x] 8.4 Dürüstlük: 8082 durdurulup rag ticket'ı tekrarlandı → raporda "MultiAgentRagRepoAgent ajanına ulaşılamadı", uydurma dosya/sınıf adı yok (rapor bunu açıkça belirtiyor) ✅
- [x] 8.5 Hafıza: backward-compatibility kuralı öğretildi → `memory/feedback_backward_compatibility.md` + `MEMORY.md` oluştu; `git status`'ta görünmüyor; yeni API ticket'ında kural rapora yansıdı (backward-compat riskleri + özel testler) ✅
- [x] 8.6 Boş gövde → 400 ✅

## Uygulama notları (plandan sapmalar ve bulgular)

1. **agent-utils paket çözümlemesi (Görev 5.4):** `SubagentReference`, `SubagentType`, `SubagentExecutor` vb. generic tipler ayrı `spring-ai-agent-utils-common:0.10.0` jar'ında, paket `org.springaicommunity.agent.common.task.subagent`. Diğer FQCN'ler plandaki gibi.
2. **Özel A2A executor (plan Riskler bölümündeki fallback devreye girdi):** Stok `A2ASubagentExecutor` (0.10.0), sunucudan gelen ilk task event'inde (WORKING, artifact yok) boş yanıt dönüyor; sunucu 0.3.0 yanıt sonrası event kuyruğunu kapattığından görev sonucu task store'a hiç yazılmıyor (polling de imkânsız). Çözüm: `coordinator/a2a/BlockingA2aSubagentExecutor` — `configuration.blocking=true` ile JSON-RPC `message/send`, tamamlanmış görevin artifact metnini döndürür. Birim testli (`BlockingA2aSubagentExecutorTest`).
3. **Sunucu timeout ayarı:** A2A SDK varsayılanı `a2a.blocking.agent.timeout.seconds=30`, LLM'li araç döngüsü için yetersiz → repo-agent `application.properties`'te 300'e çıkarıldı.
4. **Silinen test (Görev 6.7):** `CoordinatorApplicationTests.contextLoads` — plan'ın öngördüğü eager agent-card çekme durumu gerçekleşti; plan talimatı gereği silindi. Sonuç: koordinatör, repo ajanları ayaktayken başlatılmalı (README'de belirtildi).
5. **Gözlemlenebilirlik:** `coordinator/application.properties`'e `logging.level.org.springaicommunity.agent=DEBUG` eklendi (A2A resolver keşif logları görünür; TaskTool/Claude subagent çağrıları kütüphane tarafından loglanmıyor).
6. **Ortam:** Tüm Maven komutları `JAVA_HOME=~/.sdkman/candidates/java/21.0.2-tem` ile çalıştırılmalı (`.sdkmanrc` bu sürümü istiyor; sistem varsayılanı Java 17).

## Notlar

- Commit politikası: kullanıcı istemeden commit yok ([plan.md — Commit politikası](plan.md#commit-politikası)). Bu çalışmada commit YAPILMADI; tüm dosyalar working tree'de.
- Riskler ve fallback'ler için: [plan.md — Riskler](plan.md#riskler).
