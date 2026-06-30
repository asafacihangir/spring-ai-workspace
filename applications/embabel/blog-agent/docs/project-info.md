# Blog Agent — Proje Q&A

---

## S: Bu projenin amacı nedir?

Verilen bir konu hakkında **otomatik olarak blog yazısı üreten** bir AI agent sistemi inşa etmek. Proje, Embabel Agent Framework ve Spring Boot kullanarak üç aşamalı bir pipeline kurar:

1. **Outline oluşturma** — Konu için başlık ve 4-6 bölüm başlığı üretir
2. **Draft yazma** — Outline'a dayalı olarak tam bir Markdown blog yazısı yazar
3. **Review & düzenleme** — Farklı bir LLM modeli ile yazıyı gözden geçirip iyileştirir

Sonuç olarak, gözden geçirilmiş blog yazısı otomatik olarak `.md` dosyası şeklinde diske kaydedilir.

---

## S: Bu projeden öğrendiğim temel kavramlar nelerdir?

### 1. GOAP (Goal-Oriented Action Planning)
Action'ların sıralamasını **açıkça kodlamaya gerek yok**. Framework, her metodun input/output type'larını analiz ederek execution plan'ı **otomatik çıkarıyor**. Bu, oyun geliştirmede NPC davranış planlamasında kullanılan bir AI tekniğidir.

### 2. Type-Driven Workflow Orchestration
Method signature'ları aynı zamanda bir **dependency graph** tanımlıyor:
```
UserInput → BlogOutline → BlogDraft → ReviewedPost
```
Yeni bir adım eklemek için (örneğin SEO optimizasyonu) sadece doğru type'ları alan ve dönen bir method yazmak yeterli — framework sıralamayı kendisi ayarlıyor.

### 3. Multi-LLM Strategy
Yazma ve review için **farklı LLM modelleri** kullanılıyor (`gpt-5-mini` yazım için, `gpt-5` review için). Bu, maliyet ve kalite arasında bilinçli bir denge kurmayı öğretiyor.

### 4. Persona-Based Prompt Engineering
`PromptContributor` pattern'i ile agent'ın system prompt'una **rol ve kişilik** enjekte ediliyor (WRITER persona ve REVIEWER persona). Bu, prompt'ları modüler ve yeniden kullanılabilir hale getiriyor.

### 5. Spring Boot + AI Agent Entegrasyonu
`@ConfigurationProperties`, `application.yaml`, ve Embabel annotation'ları (`@Agent`, `@Action`, `@AchievesGoal`) bir arada kullanılarak production-ready bir yapı kuruluyor.

---

## S: Bu çalışmadan ne kazanırım?

- **AI Agent mimarisi deneyimi** — Sadece tek bir API çağrısı değil, çok adımlı, hedef odaklı bir agent pipeline'ı tasarlama ve uygulama becerisi.
- **Declarative workflow tasarımı** — İmperatif `if/then/call` zinciri yerine, type signature'lar üzerinden otomatik planlama yapan sistemler kurma anlayışı.
- **Embabel Framework bilgisi** — Spring ekosisteminde AI agent'lar için gelişmekte olan bir framework'ün pratik kullanım deneyimi.
- **Prompt mühendisliği pattern'leri** — Persona ayrımı, role-based prompting ve structured output (record class'lar ile) tekniklerinin uygulamalı öğrenimi.
- **LLM orkestrasyon stratejisi** — Farklı görevler için farklı modeller seçme, maliyet/performans dengesini yönetme pratiği.

---

## S: Pratik olarak bu proje bana ne fayda sağlar?

| Alan | Fayda |
|------|-------|
| **Portföy** | Spring Boot + AI Agent entegrasyonunu gösteren somut bir proje |
| **Mimari düşünce** | GOAP gibi ileri seviye planning pattern'lerini anlatabilme yetkinliği |
| **Yeniden kullanılabilirlik** | Aynı GOAP + Persona pattern'i ile farklı agent'lar üretebilme (örn. kod review agent'ı, dokümantasyon agent'ı) |
| **Production readiness** | Configuration properties, error handling, file I/O, logging gibi production kaygılarını AI agent'larla birleştirme deneyimi |
| **Mülakat konuşması** | "Type-driven workflow", "backward chaining", "multi-LLM orchestration" gibi kavramları gerçek bir proje üzerinden açıklayabilme |

---

---

## S: Execution order nasıl garanti altına alınıyor? Bu sıralama nerede ve nasıl tanımlanıyor?

## GOAP (Goal-Oriented Action Planning) ile Otomatik Sıralama

Execution order **açıkça (explicitly) tanımlanmıyor**. Bunun yerine Embabel framework'ü **GOAP** (Goal-Oriented Action Planning) kullanarak sırayı **otomatik olarak çıkarıyor** (infers).

Mekanizma şöyle çalışır:

### 1. Type Signature Zinciri

Her `@Action` metodunun **input type** ve **return type**'ı bir zincir oluşturur:

```
UserInput → createOutline() → BlogOutline
BlogOutline → writeDraft() → BlogDraft  
BlogDraft → reviewDraft() → ReviewedPost
```

- `createOutline(UserInput, Ai)` → `BlogOutline` döner
- `writeDraft(BlogOutline, Ai)` → `BlogDraft` döner
- `reviewDraft(BlogDraft, Ai)` → `ReviewedPost` döner

### 2. @AchievesGoal ile Hedef Tanımı

Satır 76'da:
```java
@AchievesGoal(description = "A reviewed and polished blog post")
```

Bu annotation GOAP engine'e **son hedefin** `ReviewedPost` üretmek olduğunu söyler. Engine bu hedeften **geriye doğru** çalışarak (backward chaining) hangi action'ların hangi sırayla çalışması gerektiğini hesaplar.

### 3. GOAP Engine'in Çıkarım Süreci

```
Hedef: ReviewedPost gerekiyor
  → reviewDraft() bunu üretir, ama BlogDraft gerekiyor
    → writeDraft() bunu üretir, ama BlogOutline gerekiyor
      → createOutline() bunu üretir, UserInput zaten mevcut ✓
```

**Sonuç:** Sıralama hiçbir yerde `1, 2, 3` diye yazılmıyor. Framework, method signature'larındaki **type dependency graph**'ı analiz ederek ve `@AchievesGoal` hedefinden geriye doğru çalışarak execution plan'ı runtime'da otomatik oluşturuyor. Bu yaklaşım, yeni bir action eklediğinizde (örneğin SEO optimizasyonu) sadece doğru input/output type'larını tanımlamanızın yeterli olmasını sağlıyor — sıralamayı framework kendisi ayarlıyor.