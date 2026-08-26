# a2a — Değişiklik Etki Analizi Asistanı

Bir feature isteği veya ticket geldiğinde local repository'leri analiz edip etkilenen bileşenleri, riskleri ve test planını içeren bir Markdown etki analizi üreten çok-ajanlı Spring AI örneğidir.

Üç Spring AI kavramını tek uygulamada birleştirir:

1. **A2A protokolü** — repository uzmanları ayrı servis olarak sunulur (`spring-ai-a2a`).
2. **TaskTool** — koordinatör; uzak (A2A) ve yerel (Markdown) alt ajanları tool olarak çağırır (`spring-ai-agent-utils`).
3. **Uzun süreli hafıza** — takımın kalıcı mimari kuralları `AutoMemoryToolsAdvisor` ile dosya tabanlı saklanır.

```text
                        POST /analyze (ticket, text/plain)
                                      │
                                      ▼
                        ┌─────────────────────────────┐
                        │  coordinator (port 8080)    │
                        │  EngineeringCoordinatorAgent│
                        │  TaskTool + hafıza          │
                        └──────┬───────────┬──────────┘
              A2A (uzak)       │           │       yerel Markdown alt ajanları
        ┌──────────────────────┤           ├─────────────────────────┐
        ▼                      ▼           ▼                         ▼
┌────────────────┐   ┌────────────────┐  ┌──────────────┐  ┌───────────────────┐
│ repo-agent     │   │ repo-agent     │  │ test-planner │  │ security-reviewer │
│ :8081          │   │ :8082          │  │ (her zaman,  │  │ (yalnızca güvenlik│
│ todowritetool  │   │ multi-agent-   │  │  kanıttan    │  │  etkileniyorsa)   │
│ repo'su        │   │ rag-spring     │  │  sonra)      │  └───────────────────┘
└──────┬─────────┘   └──────┬─────────┘  └──────────────┘
       │ listFiles / readFile / searchInFiles (sandbox'lı)
       ▼                    ▼
  applications/       applications/
  todowritetool       multi-agent-rag-spring
```

---

## Kullanılan teknolojiler

* Java 21
* Spring Boot 4.1
* Spring AI 2.0
* `org.springaicommunity:spring-ai-a2a-server-autoconfigure` 0.3.0 (A2A sunucu tarafı)
* `org.springaicommunity:spring-ai-agent-utils` + `spring-ai-agent-utils-a2a` 0.10.0 (TaskTool, A2A istemci tarafı, AutoMemoryToolsAdvisor)
* OpenAI API

---

## Nasıl çalışır?

1. `POST /analyze` ticket metnini alır; koordinatör `ChatClient` çağrılır.
2. `AutoMemoryToolsAdvisor` hafızadaki kalıcı mimari kuralları prompt'a ekler (ilk çalıştırmada boş — normal durum).
3. LLM, ticket'a göre yalnızca ilgili repo ajan(lar)ını TaskTool üzerinden çağırır. A2A ajanı kendi tarafında `listFiles` / `readFile` / `searchInFiles` araçlarıyla kanıt toplar; her bulguda dosya yolu/sembol bulunan bir rapor döner.
4. Değişiklik kimlik, yetki, kişisel veri veya dış girdiye dokunuyorsa `security-reviewer`; ardından her durumda (kanıtlar toplandıktan sonra) `test-planner` çağrılır.
5. LLM tüm bulguları sabit Türkçe şablona birleştirir; endpoint `text/markdown` döner: **İstek / Etkilenen bileşenler / Önerilen akış / Riskler / Test planı / Açık kararlar**.

`repo-agent` tek parametrik codebase'dir: aynı jar, iki Spring profiliyle iki ayrı A2A servisi olarak çalışır. Dosya araçları repo köküne sandbox'lıdır: kök dışına çözümlenen her path reddedilir, 50KB üstü dosyalar kırpılır, `target/`, `node_modules/`, `.git/` hariç tutulur.

---

## Kurulum ve çalıştırma

Proje için JDK 21 gerekir (`.sdkmanrc`: `21.0.2-tem`).

Önce OpenAI API anahtarını tanımlayın:

```bash
export OPENAI_API_KEY=sk-...
```

Üç ayrı terminalde, **bu sırayla** başlatın (koordinatör açılışta agent card'ları çektiği için repo ajanları ayakta olmalıdır):

```bash
# Terminal 1 — todowritetool repo ajanı (8081)
cd applications/a2a/repo-agent
./mvnw spring-boot:run -Dspring-boot.run.profiles=todowritetool

# Terminal 2 — multi-agent-rag-spring repo ajanı (8082)
cd applications/a2a/repo-agent
./mvnw spring-boot:run -Dspring-boot.run.profiles=rag

# Terminal 3 — koordinatör (8080)
cd applications/a2a/coordinator
./mvnw spring-boot:run
```

> **Not:** `repo-agent` kendi dizininden çalıştırılmalıdır; analiz edilecek repo kökü (`repo.agent.root`) bu dizine göre relative tanımlıdır.

---

## Kullanım

### Agent card kontrolü

```bash
curl -s http://localhost:8081/.well-known/agent-card.json | python3 -m json.tool
curl -s http://localhost:8082/.well-known/agent-card.json | python3 -m json.tool
```

### Etki analizi isteği

```bash
curl -s -X POST http://localhost:8080/analyze \
  -H 'Content-Type: text/plain' \
  --data "todowritetool'daki todo öğelerine priority alanı ekleyelim — etkilenen sınıfları, riskleri ve test senaryolarını çıkar."
```

Yanıt `text/markdown` olarak döner ve altı bölümlü şablonu içerir. Boş gövde `400 Bad Request` döndürür.

Terminal 1'de tool aktivitesi görülür (yalnızca ilgili repo ajanı çağrılır; bu ticket'ta Terminal 2 sessiz kalır).

### Hafıza demosu

Koordinatöre kalıcı bir mimari kural öğretin:

```bash
curl -s -X POST http://localhost:8080/analyze \
  -H 'Content-Type: text/plain' \
  --data "Takım kuralı olarak aklında tut: API değişikliklerinde backward compatibility korunmalı."
```

Kural `applications/a2a/coordinator/memory/` altında dosya olarak saklanır (gitignore'ludur). Sonraki API temalı analizlerde bu kural rapora kendiliğinden yansır; uygulama yeniden başlatılsa bile kalıcıdır.

---

## Proje yapısı

```text
applications/a2a/
├── docs/                  vision.md, spec.md, plan.md, tasks.md
├── repo-agent/            RepositoryAnalysisAgent — parametrik A2A sunucusu
│   ├── config/RepoAgentProperties    repo.agent.* ayarları (record)
│   ├── config/A2aServerConfig        AgentCard + AgentExecutor bean'leri
│   └── tools/RepositoryTools         listFiles / readFile / searchInFiles (sandbox)
└── coordinator/           EngineeringCoordinatorAgent — port 8080
    ├── agents/                       test-planner.md, security-reviewer.md
    ├── a2a/BlockingA2aSubagentExecutor  blocking JSON-RPC message/send ile uzak ajan çağrısı
    ├── config/CoordinatorProperties  coordinator.* ayarları (record)
    ├── config/ChatClientConfig       TaskTool + AutoMemoryToolsAdvisor wiring
    └── analyze/                      AnalyzeController (POST /analyze) + AnalyzeService
```

**Neden özel bir A2A executor var?** `spring-ai-agent-utils` 0.10.0'ın stok `A2ASubagentExecutor`'ı, sunucudan gelen ilk task event'inde (henüz `WORKING`, artifact yok) sonucu döndürüyor; sunucu (`spring-ai-a2a-server` 0.3.0) da yanıtı döndükten sonra event kuyruğunu kapattığı için görev sonucu task store'a hiç yazılmıyor. Çözüm: `BlockingA2aSubagentExecutor`, `configuration.blocking=true` ile JSON-RPC `message/send` gönderir ve tamamlanmış görevin artifact metnini döndürür. Bunun çalışması için repo-agent tarafında `a2a.blocking.agent.timeout.seconds=300` ayarlanmıştır (SDK varsayılanı 30 sn; LLM'li araç döngüsü için yetersiz).

`ChatClientConfig` içinde alt ajan `ChatClient.Builder`'ı, koordinatör default'ları eklenmeden **önce** `clone()` ile alınır; böylece alt ajanlar TaskTool'u ve hafızayı miras almaz.

---

## Testler

```bash
cd applications/a2a/repo-agent && ./mvnw test
cd applications/a2a/coordinator && ./mvnw test
```

* `RepositoryToolsTest` — sandbox trust boundary birim testleri: path traversal reddi, 50KB kırpma, hariç tutulan dizinler, arama formatı ve 200 satır cap'i.
* `RepoAgentApplicationTests.contextLoads` — repo-agent Spring context'i dummy API key ile yükleniyor.
* `AnalyzeControllerTest` — boş/null ticket gövdesi `400 Bad Request`.
* `BlockingA2aSubagentExecutorTest` — A2A JSON-RPC yanıt ayrıştırma: tamamlanmış görev artifact'ları, doğrudan mesaj yanıtı, tamamlanmamış durum ve JSON-RPC hata gövdesi.

Koordinatör için context testi bilinçli olarak yoktur: `A2ASubagentResolver` agent card'ları açılışta çektiğinden test, repo ajanları kapalıyken her zaman başarısız olurdu. LLM davranışı birim testlenmez; uçtan uca doğrulama yukarıdaki curl senaryolarıyla manuel yapılır.

---

## Kapsam dışı

* GitHub/Jira entegrasyonu, vektör veritabanı
* TestPlanning/SecurityReview'ın ayrı A2A servisi olması
* Streaming yanıt, kimlik doğrulama, retry/fallback
* Hafızanın önceden tohumlanması (kurallar konuşmayla öğretilir)
