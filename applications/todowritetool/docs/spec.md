# TodoWriteTool — Karşılaştırmalı Analiz Agent'ı (Tasarım)

**Tarih:** 2026-06-20
**Proje:** `applications/todowritetool`
**Amaç:** Spring Boot 4 + Spring AI 2 ile *Agentic Planning with TodoWriteTool* desenini, kendi içinde tamamlanan çok adımlı bir karşılaştırmalı analiz görevi üzerinde deneyimlemek.

## Bağlam

`TodoWriteTool` (`org.springaicommunity.agent.tools.TodoWriteTool`, `spring-ai-agent-utils:0.7.0`), Claude Code'un TodoWrite aracının Spring AI portudur. Agent çok adımlı (3+) bir görevi kendi planlar, planı `pending → in_progress → completed` durumlarıyla takip eder ve her güncelleme `TodoEventHandler` callback'i ile dışarı akar.

Referans blog'lar TodoWriteTool'u **dış araçlarla (filesystem/shell) birleştirmez**; senaryo saf akıl-yürütme tabanlı çok adımlı bir görevdir ve aracın değeri planı *görünür* kılmaktır:
- Spring blog: "En iyi 10 Tom Hanks filmini bul, ikişerli grupla, başlıkları ters çevir + özet"
- Medium: "Apollo 11, 13, 17'yi karşılaştır, adımlara böl, final rapor üret"

Bu spec, Medium örneğine paralel **karşılaştırmalı analiz** senaryosunu seçer.

## Senaryo

Kullanıcı karşılaştırılacak öğeleri (+ opsiyonel kriter) verir. Agent `TodoWriteTool` ile kendi planını çıkarır (örn. kriter belirle → her öğeyi analiz et → karşılaştır → final rapor), planı adım adım ilerletir (konsola loglanır) ve sonunda markdown bir karşılaştırma raporu döner.

## Mimari

Mevcut `skillsjar` deseniyle birebir:

```
config/ChatClientConfig     → ChatClient + TodoWriteTool (default tool) + loglayan TodoEventHandler
analysis/AnalysisService    → görevi prompt'a çevirir, chatClient.call() ile çalıştırır, raporu döner
analysis/AnalysisController → POST /analyze, record DTO'lar
```

### Bileşenler

1. **`pom.xml`** — tek bağımlılık eklenir:
   `org.springaicommunity:spring-ai-agent-utils:0.7.0` (skillsjar'da kanıtlı).

2. **`ChatClientConfig`**
   - `ChatClient`'a system prompt verilir: rol = karşılaştırmalı analiz asistanı; görev 3+ adım olduğundan **önce `TodoWrite` ile planla**, sonra adımları yürüt, en sonda markdown karşılaştırma raporu üret.
   - `TodoWriteTool.builder().todoEventHandler(...).build()` `defaultTools(...)` olarak eklenir.
   - `TodoEventHandler` her plan güncellemesinde konsola ilerleme basar: her todo için durum işareti (`✓` completed / `▶` in_progress / `☐` pending) + tamamlanma yüzdesi.

3. **Tool-calling döngüsü tek `.call()` içinde döner** — agent tek istek içinde `TodoWrite`'ı defalarca çağırır (Spring AI tool-calling loop). Bu yüzden chat memory / `MessageChatMemoryAdvisor` **eklenmez** (o, turlar arası kalıcılık içindir; senaryo tek atışlıktır).

4. **`AnalysisService`**
   - Constructor: `ChatClient`.
   - `analyze(List<String> items, String criteria)` → `items` ve (varsa) `criteria`'yı user prompt'a gömer, `chatClient.prompt().user(...).call().content()` ile final markdown raporu alır, döner.

5. **`AnalysisController`**
   - `POST /analyze`
   - `record AnalysisRequest(List<String> items, String criteria)`
   - `record AnalysisResponse(String report)`

### Veri akışı

```
POST /analyze {items, criteria}
  → AnalysisService.analyze()
    → chatClient.call()  (system prompt agent'ı TodoWrite ile planlamaya yönlendirir)
        ↺ agent TodoWrite'ı tekrar tekrar çağırır → TodoEventHandler konsola ilerleme loglar
      → final markdown rapor
  → AnalysisResponse {report}
```

## Hata yönetimi

- Kütüphane todo'ları zaten valide eder (tek `in_progress`, boş içerik/`activeForm` yasak, geçerli status).
- Trust boundary kontrolü: `items` null/boş gelirse `400 Bad Request`.
- `criteria` opsiyoneldir (null/boş olabilir); prompt buna göre koşullu kurulur.

## Test

Ponytail: tek anlamlı, çalışır check.

- **İlerleme formatlayıcı unit testi**: `TodoEventHandler`'ın kullandığı saf formatlama fonksiyonu (todo listesi → durum işaretli satırlar + yüzde) izole test edilir. Bu, projedeki tek non-trivial kendi kodumuz.
- **`contextLoads`**: offline geçmesi için test'te dummy `spring.ai.openai.api-key` property'si set edilir.
- LLM çağrısı testlenmez (canlı API gerektirir).

## Konfigürasyon

`application.properties`:
- `spring.application.name=todowritetool`
- `spring.ai.openai.api-key=${OPENAI_API_KEY}`
- model seçimi (skillsjar varsayılanı ile uyumlu; en güncel uygun model)
- TodoWrite ilerlemesinin görünür olması için ilgili logger seviyeleri (gürültüyü kısma skillsjar'daki gibi)

## Kapsam dışı (gerekirse sonra eklenir)

- SSE / streaming ilerleme yayını
- Chat memory / turlar arası kalıcılık
- filesystem / shell / web araçları
- Kalıcı depolama, kimlik doğrulama