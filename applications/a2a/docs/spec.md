# Tasarım: Yazılım Değişiklik Etki Analizi Asistanı (A2A)

> Onaylanmış tasarım dokümanı. Kaynak: [vision.md](vision.md). Tarih: 2026-07-18.
> Bu doküman, vizyondaki MVP'nin bu workspace'e uyarlanmış, uygulanabilir hâlidir.

## Amaç

Bir feature isteği veya ticket geldiğinde local repository'leri analiz edip
etkilenen bileşenleri, riskleri ve test planını içeren bir Markdown etki
analizi üreten çok-ajanlı sistem. Üç Spring AI kavramını birleştirir:

1. **A2A protokolü** — repository uzmanları ayrı servis olarak sunulur
   (`spring-ai-a2a`).
2. **TaskTool** — koordinatör, uzak (A2A) ve yerel (Markdown) alt ajanları
   tool olarak çağırır (`spring-ai-agent-utils`).
3. **Uzun süreli hafıza** — takımın kalıcı mimari kuralları
   `AutoMemoryToolsAdvisor` ile dosya tabanlı saklanır.

## Kapsam Kararları

| Karar | Sonuç |
|---|---|
| Ajan kapsamı | Koordinatör + 2 repository A2A ajanı + hafıza. TestPlanning ve SecurityReview ayrı servis değil, koordinatör içinde **yerel Markdown alt ajanı**. |
| Analiz edilecek repo'lar | Fixture değil, workspace'teki gerçek projeler: `applications/todowritetool` ve `applications/multi-agent-rag-spring`. |
| Repo ajanı yapısı | **Tek parametrik codebase** (`repo-agent/`); aynı jar iki profille iki örnek olarak çalışır. |
| Mevcut `demo/` iskeleti | Silinir (commit edilmemiş Initializr çıktısı). |

## Yerleşim

```
applications/a2a/
├── docs/
│   ├── vision.md
│   └── spec.md            (bu doküman)
├── coordinator/           EngineeringCoordinatorAgent — port 8080
└── repo-agent/            RepositoryAnalysisAgent — parametrik, tek codebase
```

## Bileşenler

### repo-agent (A2A sunucusu)

Spring Boot uygulaması; `spring-ai-a2a` ile A2A sunucusu olarak açılır
(`/.well-known/agent-card.json` otomatik yayınlanır). Gerekli bean'ler:
`ChatClient`, `AgentCard`, `AgentExecutor`.

Profil başına config (`application-<profil>.yml`):

| Profil | Port | Repo kökü | Ajan adı |
|---|---|---|---|
| `todowritetool` | 8081 | `../../todowritetool` | TodoWriteToolRepoAgent |
| `rag` | 8082 | `../../multi-agent-rag-spring` | MultiAgentRagRepoAgent |

Config alanları: port, ajan adı, `AgentCard` açıklaması (koordinatörün doğru
ajanı seçmesini sağlayan metin), analiz edilecek repo'nun kök dizini.

LLM'e verilen üç `@Tool` (hepsi config'teki repo köküne sandbox'lı):

- `listFiles` — dizin ağacı (target/, node_modules/, .git/ hariç)
- `readFile` — dosya içeriği; ~50KB üstü kırpılır ve kırpıldığı belirtilir
- `searchInFiles` — dosyalarda metin arama (basit substring/regex taraması)

Güvenlik (trust boundary): repo kökü dışına çözümlenen her path reddedilir
(canonical path kontrolü). Bu davranış birim testlidir.

Sistem prompt'u özü: "Repository analiz uzmanısın. Sana verilen soruyu
araçlarla repo'yu inceleyerek yanıtla. Her bulgu için dosya yolu ve sembol
göster. Kodda bulamadığın hiçbir sınıf/dosya hakkında tahmin yürütme;
bulamadıysan bulamadığını söyle."

### coordinator (EngineeringCoordinatorAgent)

Spring Boot WebMVC uygulaması, port 8080. Tek endpoint:

```
POST /analyze
Content-Type: text/plain        (gövde: ticket / feature isteği metni)
→ 200, text/markdown            (etki analizi raporu)
```

`ChatClient` kurulumu:

1. **TaskTool** — 4 alt ajan tanımı:
   - `todowritetool-repo` → uzak A2A, agent card URL'i config'ten (8081)
   - `rag-repo` → uzak A2A, agent card URL'i config'ten (8082)
   - `test-planner` → yerel Markdown alt ajanı (`resources/agents/test-planner.md`):
     risk bazlı unit/integration/contract/regresyon test planı üretir
   - `security-reviewer` → yerel Markdown alt ajanı
     (`resources/agents/security-reviewer.md`): yetkilendirme, veri sızıntısı,
     saldırı yüzeyi, loglarda hassas veri incelemesi
2. **AutoMemoryToolsAdvisor** — hafıza dizini `coordinator/memory/`
   (gitignore'lu). Kalıcı mimari kurallar burada birikir; build durumu, PR
   listesi gibi değişken veriler hafızaya alınmaz (advisor'ın kendi sistem
   prompt'u + koordinatör prompt'undaki yönlendirme).

Koordinatör sistem prompt'unun kodladığı kurallar (vizyondan):

- Kod hakkında tahmin yürütme; kanıtı ilgili repo ajanından iste.
- Ticket'ın içeriğine göre yalnızca ilgili repo ajan(lar)ını çağır.
- `security-reviewer`'ı yalnızca kimlik, yetki, kişisel veri veya dış girdi
  etkileniyorsa çağır (koşullu delegasyon).
- `test-planner`'ı her analizde, repo kanıtları toplandıktan **sonra** çağır.
- Ajan bulguları çelişirse gizleme; "Açık kararlar" bölümünde raporla.
- Çıktıyı şu Markdown şablonuyla üret: **İstek / Etkilenen bileşenler /
  Önerilen akış / Riskler / Test planı / Açık kararlar**. Her teknik bulguda
  dosya yolu veya sembol referansı bulunmalı.
- Kod değiştirme; yalnızca insan tarafından incelenecek plan üret.

## Veri Akışı

1. `POST /analyze` ticket metnini alır; koordinatör `ChatClient` çağrılır.
2. `AutoMemoryToolsAdvisor` hafızadaki kalıcı kuralları prompt'a ekler
   (ilk çalıştırmada boş — normal durum).
3. LLM ticket'a göre ilgili repo ajan(lar)ını TaskTool ile çağırır. A2A ajanı
   kendi tarafında `listFiles`/`readFile`/`searchInFiles` ile kanıt toplar,
   dosya yolu referanslı rapor döner.
4. Güvenlik açısından anlamlıysa `security-reviewer`, ardından her durumda
   `test-planner` çağrılır (ikisine de repo kanıtları iletilir).
5. LLM tüm bulguları şablona birleştirir; endpoint `text/markdown` döner.

## Hata Yönetimi

- Repo ajanı ayakta değilse TaskTool hatası LLM'e döner; koordinatör
  prompt'u gereği "X ajanına ulaşılamadı" diye raporlar, uydurmaz.
- Dosya araçları: repo kökü dışı path → red; okunamayan dosya → hata mesajı
  tool sonucu olarak döner; büyük dosya → kırpılır.
- Retry/fallback mekanizması yok — demo kapsamı için gereksiz.

## Test ve Doğrulama

- **Birim test:** yalnızca deterministik kritik nokta — repo-agent dosya
  araçlarının sandbox davranışı (path traversal reddi, kırpma, hariç tutulan
  dizinler). LLM davranışı birim testlenmez.
- **Uçtan uca (manuel, README'de):** 3 süreç başlatılır (repo-agent × 2
  profil + coordinator), örnek ticket curl ile gönderilir:
  > "todowritetool'daki todo öğelerine priority alanı ekleyelim — etkilenen
  > sınıfları, riskleri ve test senaryolarını çıkar."
  Çıktıda gerçek dosya yollarının geçtiği, yalnızca ilgili repo ajanının
  çağrıldığı gözlenir.
- **Hafıza demosu (README'de):** bir kural öğret ("API değişikliklerinde
  backward compatibility korunmalı"), yeni bir analizde hatırlandığını gör.

## Başarı Kriterleri (vizyondan, bu kapsama uyarlanmış)

- Koordinatör, ticket'a göre doğru repo ajanını seçiyor.
- Kodda bulunmayan sınıf/dosya uydurulmuyor; her bulguda dosya yolu/sembol var.
- `security-reviewer` yalnızca güvenlik açısından anlamlı değişikliklerde
  çağrılıyor; `test-planner` kanıtlardan sonra çağrılıyor.
- Kalıcı mimari kararlar ile güncel repo içeriği birbirine karışmıyor.
- Çelişkiler "Açık kararlar" olarak raporlanıyor.
- Sistem kod değiştirmiyor; yalnızca plan üretiyor.

## Bağımlılıklar

| Bağımlılık | Sürüm | Not |
|---|---|---|
| Spring Boot | 4.1.0 | workspace standardı |
| Spring AI BOM | 2.0.0 | |
| `spring-ai-agent-utils` BOM | 0.10.0 | TaskTool + AutoMemoryToolsAdvisor |
| `spring-ai-agent-utils-a2a` | BOM'dan | TaskTool'un A2A istemci tarafı |
| `org.springaicommunity:spring-ai-a2a-server-autoconfigure` | 0.3.0 | repo-agent sunucu tarafı (Spring Boot 4.0+, Spring AI 2.0.0-M2+ gerektirir) |
| `spring-ai-starter-model-openai` | BOM'dan | LLM sağlayıcı (`OPENAI_API_KEY`) |
| Java | 21 | |

## Kapsam Dışı (ilk sürüm)

- GitHub/Jira entegrasyonu, vektör veritabanı (vizyon MVP'si gereği).
- TestPlanning/SecurityReview'ın ayrı A2A servisi olması.
- Streaming yanıt, kimlik doğrulama, retry/fallback.
- Hafızanın önceden tohumlanması (kurallar konuşmayla öğretilir).
