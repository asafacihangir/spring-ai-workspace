# SkillsJar — Uygulama Planı

> Kaynak: [SPEC.md](../SPEC.md). Bu plan SPEC'i çalışan koda dönüştürür.
> Yaklaşım: **dikey kesitler** — her faz uçtan uca çalışan bir yol bırakır,
> fazlar arasında insan onay checkpoint'leri vardır.

## Araştırma bulguları (doğrulandı)

- **Tool API'leri** (`spring-ai-agent-utils` 0.7.0, `org.springaicommunity.agent.tools`):
  - `ShellTools.builder().build()` → `@Tool` metotları: `bash`, `bashOutput`, `killShell`.
  - `FileSystemTools.builder().build()` → `read`, `write`, `edit`.
  - `SkillsTool.builder().addSkillsResources(List<Resource>).build()` → `ToolCallback` döner.
  - Üçü de `ChatClientBuilderCustomizer` içinde `.defaultTools(...)` ile eklenir
    (kardeş `agentbehaviorwithskills` deseni).
- **PDF SkillsJar** (`com.skillsjars:anthropics__skills__pdf:2026_02_25-3d59511`) Maven Central'dan
  çözülüyor; içinde `META-INF/skills/anthropics/skills/pdf/SKILL.md` + Python scriptleri var.
- **Önkoşul (SPEC açık soru #1 cevabı):** Hikâye → yeni PDF üretimi `reportlab` (Python)
  kullanır. Yerelde **python3 3.12.6 + pypdf mevcut, ancak `reportlab` KURULU DEĞİL.**
  → Kurulum gerekecek; "önce sor" sınırı olduğu için **Checkpoint A**'da onaya sunulur.

## Bağımlılık grafiği

```
T1 (pom: PDF skill bağımlılığı)
  └─> T2 (config: model + skill resources + output.dir property)
        ├─> T3 (dikey kesit 1: POST /stories → LLM hikâye metni)   ──> Checkpoint A
        │                                                                │
        └────────────────────────────────────────────────────────────┘
              (Checkpoint A: reportlab kurulum onayı + kurulum)
                    └─> T4 (dikey kesit 2: SkillsTool+ShellTools+FileSystemTools → PDF)
                          └─> T5 (sağlamlaştırma: validasyon, dosya adı, dizin, contextLoads)
                                └─> Checkpoint B (final manuel uçtan uca doğrulama)
```

## Fazlar

### Faz 1 — Temel & dikey kesit 1 (PDF'siz çalışan endpoint)
**Hedef:** `POST /stories` bir konu alır, LLM ile hikâye üretir, metni döner. Bu, model
yapılandırması ve REST yolunun çalıştığını PDF karmaşası olmadan kanıtlar.
- T1, T2, T3.
- **Checkpoint A** (insan onayı): endpoint hikâye döndürüyor + **reportlab kurulum onayı**.

### Faz 2 — Dikey kesit 2 (tam senaryo: skill + tools → PDF)
**Hedef:** Agent, PDF skill'ini ShellTools/FileSystemTools ile çalıştırıp hikâyeyi PDF'e yazar;
endpoint dosya yolunu döner.
- T4.
- **Checkpoint B** (insan onayı): geçerli PDF üretiliyor mu.

### Faz 3 — Sağlamlaştırma
**Hedef:** Sınır durumları ve temizlik.
- T5.

## Görevler

### T1 — PDF SkillsJar bağımlılığını ekle
- **Yapılacak:** `pom.xml`'e `com.skillsjars:anthropics__skills__pdf:2026_02_25-3d59511`
  bağımlılığını ekle.
- **Acceptance:** `./mvnw dependency:tree` çıktısında artifact görünür; `./mvnw clean compile`
  başarılı.
- **Verify:** `./mvnw dependency:tree | grep anthropics__skills__pdf`
- **Bağımlılık:** yok.

### T2 — Yapılandırma (model + skill resources + output dir)
- **Yapılacak:** `application.properties`'e ekle:
  - `spring.ai.openai.api-key=${OPENAI_API_KEY}` ve model adı (`spring.ai.openai.chat.options.model`).
  - `agent.skills.resources=classpath:/META-INF/skills/**/SKILL.md` (kardeş projedeki
    `SkillsTool` builder'ın okuduğu desen).
  - `app.output.dir=./output`.
  - `.gitignore`'a `output/` ekle (yoksa).
- **Acceptance:** Property'ler `ChatClientConfig`'te `@Value`/`Resource` listesine sorunsuz bağlanır;
  uygulama ayağa kalkar.
- **Verify:** `./mvnw spring-boot:run` → context hatasız yüklenir (Ctrl-C ile kapat).
- **Bağımlılık:** T1.

### T3 — Dikey kesit 1: hikâye üreten endpoint
- **Yapılacak:**
  - `story/StoryController.java` — `POST /stories`, `record StoryRequest(String topic)` /
    `record StoryResponse(...)`.
  - `story/StoryService.java` — `ChatClient` ile konuda kısa hikâye üretir.
  - `config/ChatClientConfig.java` — minimal `ChatClient` bean'i (kardeş örnek deseni).
- **Acceptance:** `POST /stories {"topic":"su samuru"}` → 200 + hikâye metni içeren yanıt.
- **Verify:**
  ```bash
  curl -s -X POST localhost:8080/stories -H 'Content-Type: application/json' \
    -d '{"topic":"su samuru"}'
  ```
  Yanıtta konuyla ilgili anlamlı hikâye metni olmalı.
- **Bağımlılık:** T2.

### ⛳ Checkpoint A — insan onayı
- Faz 1 sonucu gözden geçirilir (endpoint hikâye döndürüyor mu).
- **reportlab kurulumu için onay alınır** (SPEC "önce sor" sınırı). Onaylanırsa:
  `pip3 install reportlab` çalıştırılır ve `python3 -c "import reportlab"` ile doğrulanır.
- Onay olmadan Faz 2'ye geçilmez.

### T4 — Dikey kesit 2: skill + tools ile PDF üretimi
- **Yapılacak:**
  - `ChatClientConfig`'e `ChatClientBuilderCustomizer` bean'leri ekle:
    `SkillsTool.builder().addSkillsResources(skillResources).build()`, `ShellTools.builder().build()`,
    `FileSystemTools.builder().build()` → `.defaultTools(...)`.
  - System prompt (kardeş örnekten uyarlanır): önce ilgili skill'i `Skill` tool ile yükle,
    talimatları birebir uygula, skill metadata'sını (base directory, dosya yolları) sızdırma.
  - `StoryService`: agent'a "konuda hikâye yaz ve PDF skill'i ile `<output.dir>/<güvenli-ad>.pdf`
    olarak kaydet" talimatı; üretilen dosya yolunu belirle.
  - `StoryController`/`StoryResponse`: `{"topic":..., "pdfPath":...}` döner.
- **Acceptance:** İstek sonrası `app.output.dir` altında geçerli bir PDF oluşur; yanıt doğru
  `pdfPath`'i içerir.
- **Verify:**
  ```bash
  curl -s -X POST localhost:8080/stories -H 'Content-Type: application/json' \
    -d '{"topic":"su samuru"}'
  # dönen pdfPath için:
  python3 -c "from pypdf import PdfReader; r=PdfReader('<pdfPath>'); print(len(r.pages),'sayfa'); print(r.pages[0].extract_text()[:200])"
  ```
  PDF açılmalı ve içeriği konuyla ilgili hikâye metni olmalı.
- **Bağımlılık:** Checkpoint A (reportlab kurulu).

### ⛳ Checkpoint B — insan onayı
- Üretilen PDF gözden geçirilir (geçerli mi, içerik doğru mu).
- Onaylanırsa Faz 3'e geçilir.

### T5 — Sağlamlaştırma
- **Yapılacak:**
  - Boş/eksik `topic` → anlamlı 4xx.
  - `app.output.dir` yoksa oluştur; dosya adını konudan güvenli üret (slug; path traversal yok).
  - `SkillsjarApplicationTests.contextLoads` yeşil kalsın (gerekirse model/tool bean'leri test
    profilinde mock'lanır — gerçek LLM çağrısı yapılmaz).
- **Acceptance:** Boş topic 4xx; dizin yokken ilk istek başarılı; `./mvnw test` yeşil.
- **Verify:**
  ```bash
  ./mvnw test
  curl -s -o /dev/null -w "%{http_code}\n" -X POST localhost:8080/stories \
    -H 'Content-Type: application/json' -d '{"topic":""}'   # 4xx beklenir
  ```
- **Bağımlılık:** T4.

### ⛳ Checkpoint final — manuel uçtan uca doğrulama (SPEC §5)
- Temiz çalıştırma: `./mvnw spring-boot:run` + smoke curl → geçerli PDF + doğru `pdfPath`.
- SPEC "Bitti tanımı" maddeleri tek tek doğrulanır.

## Riskler / notlar
- **reportlab kurulumu** zorunlu; onay alınana kadar Faz 2 bloklu (Checkpoint A).
- `ShellTools` agent'a kabuk erişimi verir → system prompt ve görev kapsamı dar tutulur;
  yazma yalnızca `app.output.dir` altına yönlendirilir (SPEC "Never" sınırı).
- `agent.skills.resources` desen değeri JAR'daki gerçek `META-INF/skills` yapısına göre T2'de
  teyit edilir (skill yüklenmezse desen ilk şüpheli).
- Model adı T2'de netleşir (SPEC açık soru #3).
