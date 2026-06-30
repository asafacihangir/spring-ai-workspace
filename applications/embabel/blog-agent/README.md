# Blog Agent

Verilen bir konu hakkinda otomatik olarak blog yazisi ureten bir AI agent sistemi. **Embabel Agent Framework** ve **Spring Boot** kullanarak uc asamali bir GOAP pipeline'i ile outline olusturma, draft yazma ve review islemlerini gerceklestirir.

## Nasil Calisir?

![GOAP Pipeline Diyagrami](../images/diagram.png)

Agent, **GOAP (Goal-Oriented Action Planning)** kullanir. Action'larin calisma sirasi acikca kodlanmaz — framework, her metodun **input/output type**'larini analiz ederek execution plan'i otomatik olusturur. `@AchievesGoal` annotation'i son hedefteki action'i isaretler, engine bu hedeften **geriye dogru** (backward chaining) calisarak tum zinciri cozumler.

## Teknoloji

| Katman | Teknoloji |
|--------|-----------|
| Framework | Spring Boot 3.5.13, Java 23 |
| AI Agent | Embabel Agent Framework 0.4.0-SNAPSHOT |
| AI Backend | Spring AI 1.1.4, OpenAI API |
| Yazim Modeli | `gpt-5-mini` (maliyet optimizasyonu) |
| Review Modeli | `gpt-5` (kalite optimizasyonu) |

## On Kosullar

- **Java 23** (`java -version` ile kontrol edin)
- **OPENAI_API_KEY** environment variable tanimli olmali

```bash
export OPENAI_API_KEY=your-key-here
```

## Kurulum ve Calistirma

```bash
cd blog-agent
./mvnw clean compile
./mvnw spring-boot:run
```

Uygulama basladiginda Embabel Shell acilir. Blog yazisi uretmek icin:

```
x Spring Boot nedir
```

Verbose mod ile prompt'lari gormek icin:

```
x -p Microservices nedir
```

Uretilen blog yazilari `blog-posts/` dizinine `.md` dosyasi olarak kaydedilir.

## Proje Yapisi

```
blog-agent/
  src/main/java/org/phoenix/blogagent/
    BlogAgentApplication.java          # Spring Boot entry point
    agent/
      BlogWriterAgent.java             # 3 action'li GOAP agent
      Personas.java                    # WRITER ve REVIEWER persona tanimlari
    config/
      BlogAgentProperties.java         # Configuration properties (output-dir)
    model/
      BlogOutline.java                 # record: title, sections
      BlogDraft.java                   # record: title, content
      ReviewedPost.java                # record: title, content, feedback
  src/main/resources/
    application.yaml                   # LLM model ve output konfigurasyonu
```

## Pipeline Asamalari

### 1. Outline Olusturma (`createOutline`)

- **Model:** `gpt-5-mini`
- **Persona:** WRITER (Software Developer and Educator)
- **Girdi:** `UserInput` (kullanicinin verdigi konu)
- **Cikti:** `BlogOutline` (baslik + 4-6 bolum basligi)

### 2. Draft Yazma (`writeDraft`)

- **Model:** `gpt-5-mini`
- **Persona:** WRITER
- **Girdi:** `BlogOutline`
- **Cikti:** `BlogDraft` (tam Markdown blog yazisi)

### 3. Review ve Duzenleme (`reviewDraft`)

- **Model:** `gpt-5`
- **Persona:** REVIEWER (Technical Editor)
- **Girdi:** `BlogDraft`
- **Cikti:** `ReviewedPost` (duzeltilmis icerik + feedback)
- Dosyayi otomatik olarak `blog-posts/` dizinine kaydeder

## Temel Kavramlar

### GOAP (Goal-Oriented Action Planning)

Execution order hicbir yerde `1, 2, 3` diye tanimlanmaz. Framework, method signature'larindaki **type dependency graph**'i analiz ederek ve `@AchievesGoal` hedefinden geriye dogru calisarak execution plan'i runtime'da olusturur.

```
Hedef: ReviewedPost gerekiyor
  -> reviewDraft() bunu uretir, ama BlogDraft gerekiyor
    -> writeDraft() bunu uretir, ama BlogOutline gerekiyor
      -> createOutline() bunu uretir, UserInput zaten mevcut
```

### Multi-LLM Strategy

Farkli gorevler icin farkli modeller kullanilir. Yazim islemi icin daha hafif ve ucuz bir model (`gpt-5-mini`), review icin daha guclu bir model (`gpt-5`) secilmistir. Bu yaklasim maliyet ve kalite arasinda bilingli bir denge kurar.

### Persona-Based Prompt Engineering

`PromptContributor` pattern'i ile her action'a farkli bir persona atanir:

- **WRITER:** Deneyimli yazilim gelistirici ve egitimci. Basit, pratik ve yeni baslayanlar icin anlasilir icerik uretir.
- **REVIEWER:** Teknik editor. Netlik, dogruluk ve sikilik acisindan yazilari gozden gecirir.

## Shell Komutlari

| Komut | Aciklama |
|-------|----------|
| `help` | Kullanilabilir komutlarin listesi |
| `models` | Tanimli LLM modelleri |
| `agents` | Kayitli agent'lar |
| `goals` | Tanimli goal'lar |
| `actions` | Mevcut action'lar |
| `x <konu>` | Belirtilen konuda blog yazisi uret |
| `x -p <konu>` | Verbose mod — prompt'lari goster |
| `exit` | Shell'den cik |

## Test Senaryolari

Detayli test adimlari icin: [`docs/testing-scenarios.md`](docs/testing-scenarios.md)

### Hizli Kontrol Listesi

| # | Test | Sonuc |
|---|------|-------|
| 1.1 | Temiz build basarili | [ ] |
| 1.2 | Uygulama basliyor | [ ] |
| 1.3 | Shell komutlari calisiyor | [ ] |
| 2.1 | Draft action calisiyor | [ ] |
| 3.1 | Uc asamali pipeline calisiyor | [ ] |
| 3.2 | Verbose mod prompt'lari gosteriyor | [ ] |
| 4.1 | Blog yazisi dosyaya kaydediliyor | [ ] |
| 4.2 | Persona bilgileri prompt'ta gorunuyor | [ ] |
| 5.1 | Ozel karakterli baslik dogru dosya adi uretiyor | [ ] |

## Konfigürasyon

`application.yaml` dosyasinda asagidaki ayarlar yapilabilir:

```yaml
blog-agent:
  output-dir: blog-posts          # blog yazilarinin kaydedilecegi dizin

embabel:
  models:
    default-llm: gpt-5-mini       # yazim icin kullanilan model
    llms:
      reviewer: gpt-5             # review icin kullanilan model
```

## Temizlik

Uretilen blog yazilarini temizlemek icin:

```bash
rm -rf blog-posts/
```

`blog-posts/` dizini `.gitignore`'da tanimlidir, repo'ya eklenmez.
