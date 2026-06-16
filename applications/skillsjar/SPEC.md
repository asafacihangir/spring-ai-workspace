# SkillsJar — Spring AI Agent Skills Spec

> Spring AI üzerinde, SkillsJar (Maven Central'da paketlenmiş Agent Skill) kullanan
> bir agent. Kullanıcı bir konu verir; agent o konuda bir hikâye yazar (LLM) ve
> JAR'dan classpath üzerinden yüklenen **PDF skill**'ini `ShellTools` + `FileSystemTools`
> ile çalıştırarak hikâyeyi bir PDF dosyasına yazar. Tetikleyici bir REST endpoint'tir.

---

## 1. Objective (Amaç)

**Ne yapıyoruz:** Bir REST endpoint'e konu (ör. "su samuru") gönderildiğinde, Spring AI
agent'ı bu konuda kısa bir hikâye üretir ve SkillsJar'dan yüklenen `anthropics__skills__pdf`
skill'ini kullanarak hikâyeyi bir PDF dosyasına dönüştürür. Üretilen PDF, yapılandırılabilir
bir çıktı dizinine yazılır ve dosya yolu JSON yanıtı olarak döner.

**Neden:** SkillsJar'ların (classpath'ten okunan Agent Skill paketleri) Spring AI ile
`SkillsTool` üzerinden uçtan uca nasıl çalıştığını gösteren çalışan bir referans örnek.

**Hedef kullanıcı:** Spring AI + Agent Skills entegrasyonunu öğrenen/değerlendiren
geliştiriciler. Production değil, demonstrasyon amaçlı bir örnek uygulama.

**Bitti tanımı (Definition of Done):**
- `POST /stories` (veya eşdeğeri) bir konu alır, geçerli bir PDF üretir ve dosya yolunu döner.
- PDF, hikâye metnini içeren, açılıp okunabilen geçerli bir dosyadır.
- Agent, PDF üretimini `SkillsTool` (PDF skill) + `ShellTools` + `FileSystemTools` üzerinden
  yapar — PDF üretim mantığı elle Java'da kodlanmaz.

---

## 2. Commands (Komutlar)

Maven wrapper (`./mvnw`) proje kökünde mevcut. Çalışma dizini:
`applications/skillsjar`.

| Amaç | Komut |
|------|-------|
| Derle | `./mvnw clean compile` |
| Testleri çalıştır | `./mvnw test` |
| Paketle | `./mvnw clean package` |
| Uygulamayı çalıştır | `./mvnw spring-boot:run` |
| Bağımlılık ağacı (skill JAR doğrulama) | `./mvnw dependency:tree` |

**Ortam değişkenleri** (`.env` / ortam üzerinden, repoya commit edilmez):
- `OPENAI_API_KEY` — OpenAI model erişimi (zorunlu).

**Manuel doğrulama (smoke):**
```bash
curl -X POST http://localhost:8080/stories \
  -H "Content-Type: application/json" \
  -d '{"topic":"su samuru"}'
# Beklenen: {"topic":"su samuru","pdfPath":"/.../output/su-samuru.pdf"}
```

---

## 3. Project Structure (Proje Yapısı)

Mevcut iskelet korunur; paket kökü `org.phoenix.skillsjar`. Kardeş örnek
(`agentbehaviorwithskills`) konvansiyonu izlenir: `chat` / `config` / `tools` katmanları.

```
applications/skillsjar/
├── pom.xml                         # Spring Boot 4.1.0, Spring AI 2.0.0, Java 21
├── SPEC.md                         # bu dosya
├── docs/idea.md                    # ham fikir / kaynak notlar
├── .env.example                    # OPENAI_API_KEY örneği
├── output/                         # üretilen PDF'ler (gitignore'lanır)
└── src/
    ├── main/java/org/phoenix/skillsjar/
    │   ├── SkillsjarApplication.java
    │   ├── story/
    │   │   ├── StoryController.java     # POST /stories — REST tetikleyici
    │   │   └── StoryService.java        # ChatClient ile agent çağrısı
    │   └── config/
    │       └── ChatClientConfig.java    # SkillsTool + ShellTools + FileSystemTools wiring
    ├── main/resources/
    │   └── application.properties       # model, skill yolu, çıktı dizini ayarları
    └── test/java/org/phoenix/skillsjar/
        └── SkillsjarApplicationTests.java
```

**Bağımlılıklar (pom.xml'e eklenecek):** mevcut `spring-ai-agent-utils` 0.7.0'a ek olarak
PDF SkillsJar:

```xml
<dependency>
    <groupId>com.skillsjars</groupId>
    <artifactId>anthropics__skills__pdf</artifactId>
    <version>2026_02_25-3d59511</version>
</dependency>
```

> Not: Custom agent (Spring AI) yolunda Maven extraction plugin'e **gerek yoktur** —
> skill'ler `SkillsTool` ile doğrudan classpath'ten (`META-INF/skills/...`) okunur.

**Yapılandırma (`application.properties`):**
- `agent.skills.resources=classpath:/META-INF/skills/**/SKILL.md` — kardeş projedeki
  `SkillsTool` builder'ın okuduğu skill kaynak deseni (gerçek desen pom çözüldükten sonra
  JAR içeriği incelenerek doğrulanır).
- `app.output.dir=./output` — PDF çıktı dizini (yapılandırılabilir).
- OpenAI model/sıcaklık ayarları `spring.ai.openai.*` altında.

---

## 4. Code Style (Kod Stili)

- **Dil/sürüm:** Java 21, Spring Boot 4.1.0, Spring AI 2.0.0.
- **Paketleme:** Feature-temelli paketler (`story`, `config`), kardeş örnekle tutarlı.
- **DTO'lar:** `record` kullan (kardeş örnekteki `ChatRequest`/`ChatResponse` gibi);
  controller iç içe record'ları içinde tanımlayabilir.
- **Bağımlılık enjeksiyonu:** Constructor injection; `@Value` ile property bağlama.
- **ChatClient kurulumu:** `ChatClientBuilderCustomizer` bean'leri ile tool'lar eklenir
  (`SkillsTool`, `ShellTools`, `FileSystemTools`); kardeş `ChatClientConfig` deseni izlenir.
- **System prompt:** Agent'a önce ilgili skill'i `Skill` tool ile yüklemesi, talimatları
  birebir uygulaması ve skill metadata'sını (örn. "Base directory ..." satırı, dosya yolları)
  yanıta sızdırmaması söylenir — kardeş örnekteki system prompt temel alınır.
- **İsimlendirme:** Sınıflar `PascalCase`, metot/değişkenler `camelCase`, property'ler
  `kebab`/`dot` (Spring konvansiyonu).
- **Girinti:** Mevcut dosyalardaki stile uy (4 boşluk / mevcut kardeş örnek formatı).
- **Sırlar:** API anahtarları asla koda gömülmez; ortam değişkeni / `.env` üzerinden gelir.

---

## 5. Testing Strategy (Test Stratejisi)

Kullanıcı kararı: **manuel doğrulama yeterli.** Gerçek LLM + PDF skill çağrıları yavaş,
maliyetli ve dış ortama (OpenAI, Python araç zinciri) bağımlı olduğundan otomatik test
minimumda tutulur.

- **Otomatik:** `SkillsjarApplicationTests` — yalnızca `contextLoads()` (Spring context
  ayağa kalkıyor mu). CI'da dış bağımlılık veya gerçek API anahtarı gerektirmez; gerekirse
  context test'i için model/tool bean'leri mock'lanır ya da test profili kullanılır.
- **Manuel (uçtan uca):** Uygulama `./mvnw spring-boot:run` ile ayağa kaldırılır, geçerli
  `OPENAI_API_KEY` ile yukarıdaki `curl` çağrısı yapılır. Doğrulama kriterleri:
  1. HTTP 200 ve geçerli bir `pdfPath` döner.
  2. Dönen yoldaki dosya mevcut ve geçerli bir PDF'tir (açılabilir).
  3. PDF içeriği verilen konuyla ilgili hikâye metnini içerir.
- **Negatif/sınır:** Boş/eksik `topic` için anlamlı 4xx; çıktı dizini yoksa oluşturulur
  veya net hata döner (manuel kontrol edilir).

---

## 6. Boundaries (Sınırlar)

**Her zaman yap (Always):**
- Skill'leri classpath'ten `SkillsTool` ile yükle; PDF üretimini agent + skill +
  `ShellTools`/`FileSystemTools` zinciriyle yaptır.
- API anahtarlarını ortam değişkeninden oku; `.env` ve `output/` dizinini `.gitignore`'da tut.
- PDF çıktısını yapılandırılabilir `app.output.dir` altına, konudan türetilen güvenli bir
  dosya adıyla yaz.
- Kardeş örnek (`agentbehaviorwithskills`) konvansiyonlarına ve mevcut pom sürümlerine sadık kal.

**Önce sor (Ask first):**
- **Sistem düzeyinde önkoşul kurulumu** (örn. Python paketleri / `pip install`, sistem
  araçları). PDF skill'in Python gerektirip gerektirmediği şu an kesin değil; pom çözüldükten
  sonra `META-INF/skills/anthropics/skills/pdf/SKILL.md` okunup gerçek önkoşullar belirlenir.
  Otomatik/sessiz kurulum yapılmaz — bulgular kullanıcıya raporlanıp onay alınır.
- `ShellTools`'a verilecek komut/dizin kapsamının genişletilmesi (sandbox dışına çıkma).
- pom'daki sürümleri (Spring Boot/AI, agent-utils, skill JAR) değiştirmek.
- REST sözleşmesini (endpoint yolu, request/response şekli) değiştirmek.

**Asla yapma (Never):**
- PDF üretim mantığını skill'i bypass ederek elle Java kütüphanesiyle kodlama (örneğin
  doğrudan bir PDF kütüphanesi çağırmak) — amaç skill entegrasyonunu göstermek.
- Sırları koda/commit'e gömme.
- `ShellTools`/`FileSystemTools` ile yapılandırılmış çıktı dizini dışına serbestçe yazma
  veya yıkıcı kabuk komutları (silme, sistem değişikliği) çalıştırma.
- Skill metadata'sını (base directory, iç dosya yolları) kullanıcı yanıtına sızdırma.

---

## Açık Sorular / Doğrulanacaklar

1. **PDF skill önkoşulu:** `anthropics__skills__pdf` çalışmak için Python/araç zinciri
   istiyor mu? → pom çözüldükten sonra JAR içindeki `SKILL.md` okunarak netleştirilecek.
2. **`agent.skills.resources` desen değeri:** Kardeş projede kullanılan tam değer/biçim,
   JAR'daki gerçek `META-INF/skills` yapısına göre teyit edilecek.
3. **Model seçimi:** `spring.ai.openai` için kullanılacak model adı (varsayılan vs. açık).
