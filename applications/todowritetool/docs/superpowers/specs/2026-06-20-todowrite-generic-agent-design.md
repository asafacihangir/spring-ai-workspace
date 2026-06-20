# TodoWriteTool — Generic Agentic-Planning Endpoint (Refinement Tasarımı)

**Tarih:** 2026-06-20
**Proje:** `applications/todowritetool`
**Önceki spec:** `docs/spec.md` (Karşılaştırmalı Analiz Agent'ı)
**Amaç:** Mevcut implementasyonu `docs/refinement.md`'deki üç geri bildirime göre revize etmek.

## Bağlam

İlk spec, TodoWriteTool desenini **karşılaştırmalı analiz** senaryosuna sabitledi ve girdiyi `List<String> items + String criteria` olarak kısıtladı. Kullanım sırasında üç sorun ortaya çıktı (`refinement.md`):

1. Yapılandırılmış girdi kullanıcıyı gereksiz kısıtlıyor; blog örnekleri (örn. "Tom Hanks filmlerini bul, ikişerli grupla, başlıkları ters çevir") serbest metin, çok-adımlı görevler — karşılaştırmalı analizle sınırlı değil.
2. Blog `MessageChatMemoryAdvisor` kullanmış; bizde yok — gerekli mi, advisor nedir?
3. Loglarda ilerleme %100'e ulaşmıyor; `@EventListener`/`TodoProgressListener` mı kullanmalıydık?

Bu spec her üçünü çözer ve projeyi **generic agentic-planning endpoint**'e dönüştürür.

## Kararlar

### 1. Serbest girdi — projeyi genelleştir

Karşılaştırmalı-analiz teması bırakılır. Agent, herhangi bir çok-adımlı (3+) görevi serbest metinden alır, `TodoWrite` ile planlar, yürütür ve sonucu döner.

- **DTO:** `AgentRequest(String message)`, `AgentResponse(String result)`.
- **Endpoint:** `POST /agent`.
- **Paket/sınıf rename:** `analysis/` → `agent/`; `AnalysisService` → `AgentService`, `AnalysisController` → `AgentController`. (`TodoProgress` aynı isimle `agent/` paketine taşınır.)
- **Service:** `run(String message)` → mesajı doğrudan geçirir:
  `chatClient.prompt().user(message).call().content()`. items/criteria birleştirme mantığı silinir.
- **Validation (trust boundary):** `message` null/blank → `400 Bad Request`.

### 2. Advisor — eklenmiyor

**Advisor nedir:** Spring AI'da advisor, `ChatClient` çağrı zincirine takılan interceptor'dür (middleware/filter gibi); istek LLM'e gitmeden ve cevap döndükten sonra araya girer. `MessageChatMemoryAdvisor` geçmiş konuşma turlarını prompt'a enjekte edip cevabı saklar = çok turlu hafıza.

**Neden yok:** Senaryo tek atışlık — tek istek içinde agent planlar, adımları yürütür (tek `.call()`'daki tool-calling döngüsü) ve sonucu döner. Turlar arası hafıza gerekmez. Çok turlu sohbet bir gereksinim olursa sonra eklenir (kapsam dışı).

### 3. İlerleme %100'e ulaşmıyor — prompt seviyesinde düzeltme

**Bulgu:** `spring-ai-agent-utils:0.7.0` jar'ı incelendi; tek callback `TodoWriteTool.TodoEventHandler.handle(Todos)` var, her `todoWrite` çağrısında tetikleniyor. Kütüphanede Spring `ApplicationEvent`/`@EventListener` mekanizması **yok** — dinlenecek bir event olmadığından `@EventListener` uygulanabilir değil. Mevcut `TodoEventHandler` doğru ve tek mekanizma.

**Gerçek sebep:** LLM son todo'yu `in_progress` yapıp nihai cevabı üretiyor, ama "hepsi completed" diyen son `TodoWrite`'ı çoğu zaman göndermiyor; bu yüzden son `handle()` %80'de kalıyor. Mimari sorun değil, model davranışı.

**Çözüm:** System prompt'a kural: nihai cevabı vermeden ÖNCE tüm todo'ları `completed` işaretleyen son bir `TodoWrite` çağrısı yap. Best-effort (LLM uyumuna bağlı); mimari değişiklik yok. `TodoProgress` formatter + logging aynen korunur (demo'nun amacı planı görünür kılmak).

## Mimari (değişmeyen iskelet)

```
config/ChatClientConfig   → ChatClient + TodoWriteTool (default tool) + loglayan TodoEventHandler
agent/AgentService        → mesajı chatClient.call() ile çalıştırır, sonucu döner
agent/AgentController      → POST /agent, AgentRequest/AgentResponse record'ları
agent/TodoProgress        → todos → durum işaretli satırlar + yüzde (saf formatlayıcı)
```

### Generic system prompt (yeni)

> You are an agent that handles multi-step tasks. For any task with 3 or more
> steps, you MUST FIRST call the `TodoWrite` tool to lay out your plan. Then
> execute the steps one at a time: mark exactly ONE todo as `in_progress` before
> starting it, and mark it `completed` immediately after finishing. Keep the todo
> list updated in real time via `TodoWrite`. Before producing your final answer,
> make a final `TodoWrite` call marking every todo as `completed`. Respond with
> ONLY the final answer (no todo metadata, no commentary about the tool).

### Veri akışı

```
POST /agent {message}
  → AgentService.run(message)
    → chatClient.prompt().user(message).call()
        ↺ agent TodoWrite'ı tekrar tekrar çağırır → TodoEventHandler konsola ilerleme loglar
      → final içerik
  → AgentResponse {result}
```

## Hata yönetimi

- Kütüphane todo'ları zaten valide eder (tek `in_progress`, boş içerik/`activeForm` yasak, geçerli status).
- Trust boundary: `message` null/blank → `400 Bad Request`.

## Test

- **`TodoProgressTest`** (mevcut): saf formatlama fonksiyonu izole test edilir — projedeki tek non-trivial kendi kodumuz. Paket taşınması dışında değişmez.
- **`contextLoads`** (mevcut): offline geçmesi için dummy `spring.ai.openai.api-key`.
- LLM çağrısı testlenmez (canlı API gerektirir).

## Konfigürasyon

`application.properties` değişmez (`todowritetool` adı, API key, logger seviyeleri korunur).

## Kapsam dışı (gerekirse sonra eklenir)

- SSE / streaming ilerleme yayını
- Chat memory / `MessageChatMemoryAdvisor` / turlar arası kalıcılık
- filesystem / shell / web araçları
- Kalıcı depolama, kimlik doğrulama