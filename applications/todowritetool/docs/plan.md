# TodoWriteTool — Karşılaştırmalı Analiz Agent'ı (Uygulama Planı)

## Context

`docs/spec.md`, Spring Boot 4 + Spring AI 2 ile *Agentic Planning with TodoWriteTool* desenini
deneyimlemek için tasarlanmış. Amaç: agent'ın çok adımlı bir karşılaştırmalı analiz görevini
kendi planlaması (`TodoWriteTool`), planı `pending → in_progress → completed` ilerletmesi ve her
güncellemeyi konsola loglaması; sonunda markdown rapor dönmesi.

Proje şu an boş bir Spring Boot scaffold'ı (`org.phoenix.todowritetool`, parent 4.1.0, Spring AI BOM
2.0.0). Sadece `TodowritetoolApplication` + minimal `application.properties` var. Eksik olan: agent-utils
bağımlılığı, config/analysis paketleri ve yapılandırma. Bu plan, kanıtlanmış `skillsjar` desenini
birebir taklit ederek (aynı parent, aynı Java 21, aynı ChatClient/Service/Controller yapısı) eksiği
tamamlar.

## Yapılacaklar

### 1. `pom.xml` — tek bağımlılık ekle
Mevcut `pom.xml`'e OpenAI starter'dan sonra ekle:
```xml
<dependency>
    <groupId>org.springaicommunity</groupId>
    <artifactId>spring-ai-agent-utils</artifactId>
    <version>0.7.0</version>
</dependency>
```
Geri kalanı (parent 4.1.0, Java 21, spring-ai-bom 2.0.0, webmvc, webmvc-test) zaten doğru.

### 2. `config/ChatClientConfig.java` (yeni)
`skillsjar/config/ChatClientConfig` deseni, sadece TodoWriteTool ile. Bileşenler:
- `@Bean ChatClient chatClient(ChatClient.Builder b)` → `b.build()`.
- `@Bean ChatClientBuilderCustomizer` → `defaultSystem(...)` + `defaultTools(todoWriteTool)`.
  - System prompt: rol = karşılaştırmalı analiz asistanı; görev 3+ adım olduğundan **önce `TodoWrite`
    ile planla** (kriter belirle → her öğeyi analiz et → karşılaştır → final rapor), adımları
    `in_progress`/`completed` ilerlet, en sonda **markdown karşılaştırma raporu** üret.
- `TodoWriteTool` bean: `TodoWriteTool.builder().todoEventHandler(this::logProgress).build()`.
- `logProgress(TodoWriteTool.Todos todos)` → `TodoProgress.format(todos)` çıktısını konsola basar
  (logger ile).

Gerçek API (doğrulandı): `TodoWriteTool.Todos(List<TodoItem> todos)`,
`TodoItem(String content, Status status, String activeForm)`, `Status { pending, in_progress, completed }`,
`TodoEventHandler.handle(Todos)`.

### 3. `analysis/TodoProgress.java` (yeni) — saf formatlama (test edilen tek non-trivial kod)
`static String format(TodoWriteTool.Todos todos)`:
- Her todo için durum işareti: `✓` completed / `▶` in_progress / `☐` pending + `content`.
- Sonda tamamlanma yüzdesi (`completed / toplam`).
- Saf fonksiyon, I/O yok → izole unit-test edilebilir. (ChatClientConfig bunu çağırıp loglar.)

### 4. `analysis/AnalysisService.java` (yeni)
- Constructor: `ChatClient`.
- `String analyze(List<String> items, String criteria)`:
  - `items`/`criteria`'yı user prompt'a göm; `criteria` boşsa koşullu olarak çıkar.
  - `chatClient.prompt().user(prompt).call().content()` → markdown rapor döndür.

### 5. `analysis/AnalysisController.java` (yeni)
- `@RestController`, `POST /analyze`.
- `record AnalysisRequest(List<String> items, String criteria)`.
- `record AnalysisResponse(String report)`.
- Trust boundary: `items` null/boş → `400 Bad Request`
  (`ResponseStatusException(HttpStatus.BAD_REQUEST, ...)`). `criteria` opsiyonel.

### 6. `application.properties` — yapılandır
```properties
spring.application.name=todowritetool          # mevcut
spring.ai.openai.api-key=${OPENAI_API_KEY}
logging.level.org.springframework.ai=WARN
logging.level.org.springaicommunity.agent.tools.TodoWriteTool=INFO
```
Model belirtilmez — skillsjar gibi OpenAI starter default'u kullanılır (spec "skillsjar varsayılanı ile
uyumlu"). Logger seviyeleri ilerleme logunun görünür, AI gürültüsünün kısık olması için ayarlanır.

### 7. Test
- `TodoProgressTest` (yeni): `format()`'ı assert-tabanlı doğrula — karışık statülü bir liste için
  doğru işaretler + doğru yüzde; boş liste kenar durumu.
- `TodowritetoolApplicationTests.contextLoads` (mevcut): offline geçmesi için dummy api-key.
  `@SpringBootTest(properties = "spring.ai.openai.api-key=test")` ekle.

## Kapsam dışı (spec gereği)
SSE/streaming, chat memory, filesystem/shell/web araçları, kalıcı depolama, auth.

## Critical files
- `pom.xml` (mevcut — düzenle)
- `src/main/java/org/phoenix/todowritetool/config/ChatClientConfig.java` (yeni)
- `src/main/java/org/phoenix/todowritetool/analysis/TodoProgress.java` (yeni)
- `src/main/java/org/phoenix/todowritetool/analysis/AnalysisService.java` (yeni)
- `src/main/java/org/phoenix/todowritetool/analysis/AnalysisController.java` (yeni)
- `src/main/resources/application.properties` (mevcut — düzenle)
- `src/test/java/org/phoenix/todowritetool/analysis/TodoProgressTest.java` (yeni)
- `src/test/java/org/phoenix/todowritetool/TodowritetoolApplicationTests.java` (mevcut — düzenle)

Referans desen (birebir taklit): `applications/skillsjar/` — `config/ChatClientConfig`,
`story/StoryService`, `story/StoryController`.

## Verification
1. `./mvnw test` → `TodoProgressTest` + `contextLoads` offline geçer (canlı API yok).
2. `OPENAI_API_KEY=... ./mvnw spring-boot:run` ile çalıştır.
3. Uçtan uca:
   ```bash
   curl -s -X POST localhost:8080/analyze -H 'Content-Type: application/json' \
     -d '{"items":["Apollo 11","Apollo 13","Apollo 17"],"criteria":"görev amacı ve sonucu"}'
   ```
   Beklenen: konsolda TodoWrite ilerleme logları (`☐/▶/✓` + yüzde), yanıtta markdown rapor.
4. Negatif: `{"items":[]}` → `400 Bad Request`.
