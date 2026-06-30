# Self-Correcting Structured Output in Spring AI 2.0




## 1. Temel Sorun: Neden "Structured Output"a İhtiyacımız Var?

- LLM'ler (large language models) **text-in, text-out** sistemlerdir. Yani girdileri de çıktıları da düz metindir (natural language).
- Düz metin **insanlar için iyi** bir arayüzdür ama **yazılım için kötüdür**. Kodun bir alana göre dallanması, bir değeri kaydetmesi gerektiğinde, sohbetin bir "kayıt" (record) haline gelmesi lazım.
- **Structured output** işte bu boşluğu doldurur: Model, belirlediğin bir **schema**'ya uyan metin üretmeye yönlendirilir. Uygulama da bu metni geri **typed object**'e (tip belirli nesneye) çevirir.

---

## 2. En Basit Hali: `.entity(...)`

- Önce istediğin şekli bir Java **record** olarak tanımlıyorsun:
  ```java
  record ActorsFilms(String actor, List<String> movies) {}
  ```
- Sonra çağrıyı `.content()` (ham metin döner) yerine `.entity(...)` ile bitiriyorsun ve hedef tipi veriyorsun. Sonuç doğrudan typed nesne oluyor.

**Arka planda 3 şey oluyor:**
1. Bir schema generator, record'unu **JSON schema**'ya çevirir.
2. Bu schema, prompt'un system context'ine eklenir.
3. Modelin JSON cevabı bir **type converter** ile geri record'a çevrilir.

**Önemli sınır:** `.entity(...)` sadece `.call()` ile çalışır. **Streaming (`.stream()`) ile çalışmaz**, çünkü tipli parsing için cevabın tamamı gerekir.

**En kritik nokta:** Bu yöntemin **hiçbir garantisi yoktur**. Model JSON üretmeye *teşvik edilir* ama *zorlanmaz*. Bazen fazladan alan ekler, zorunlu bir alanı atlar veya JSON'ı düz yazıyla sarar. O zaman parser **hata fırlatır** (throws).

---

## 3. İlk Çözüm — Cevap Tarafı: `validateSchema()` (Güvenlik Ağı)

Spring AI 2.0'ın getirdiği ilk yeni "düğme". Tek satırla açılır:
```java
.entity(ActorsFilms.class, spec -> spec.validateSchema());
```

**Nasıl çalışır (self-correcting retry loop):**
1. Model cevap verir.
2. Spring AI cevabı schema'ya göre doğrular (validate).
3. Geçerse → typed record'u alırsın.
4. Geçmezse → **hatanın kendisi** (örn. "missing required field actor") prompt'a eklenir ve çağrı tekrar yapılır — **varsayılan 3 deneme**.

En güzel yanı: İkinci deneme **kör bir tekrar değildir**. Model neyin yanlış olduğunu görür ve düzeltebilir.

- Bunu `StructuredOutputValidationAdvisor` yönetir; `validateSchema()` çağırınca **otomatik** kaydolur.
- İstersen kendin yapılandırabilirsin (örn. `.maxRepeatAttempts(5)`). Kendi yazdığın advisor, otomatik olanın yerine geçer.

---

## 4. İkinci Çözüm — İstek Tarafı: `useProviderStructuredOutput()`

İkinci yeni düğme. Bu, **cevaptan sonra** değil, **istek sırasında** önlem alır: Modelin sağlayıcısına (provider) API seviyesinde "cevap mutlaka şu schema'ya uymalı" der.
```java
.entity(ActorsFilms.class, spec -> spec.useProviderStructuredOutput());
```

**Wire (ağ) seviyesinde ne değişir:**
- System prompt'ta artık JSON format talimatı yok → **daha temiz, daha az token**.
- Schema, provider'a **API alanı** olarak gönderilir.
- Provider'ın kendi runtime'ı uyumu **zorlar** → geçersiz cevap zaten üretilemez.

**Desteklenen provider'lar (2.0):** OpenAI, Anthropic, Google GenAI, Mistral AI, Ollama (modele bağlı). Aynı çağrı, hangisi bağlıysa onunla çalışır.

- Spring AI desteği nasıl anlar? Modelin chat options'ı `StructuredOutputChatOptions` interface'ini uyguluyor mu diye bakar. Uygulamıyorsa flag **sessizce yok sayılır** ve prompt tabanlı varsayılana döner.

**Neden varsayılan olarak kapalı?** → **Uyumluluk (compatibility)**. Eski/desteklemeyen modeller isteği reddederdi; prompt tabanlı yöntem ise her yerde çalışır.

**Bilinmesi gereken sınırlar:**
- **Kısmi JSON Schema desteği:** Destekleyen provider'larda bile `$ref`, derin iç içe diziler, `allOf`/`anyOf`/`oneOf`, regex, recursive tipler gibi şeyler çoğu zaman çalışmaz.
- **Ollama'da reasoning ("thinking") modelleri** (örn. qwen): İç düşünce izini JSON yerine düz metin olarak basabilir → parse hatası.
- **OpenAI top-level array kabul etmez.** Bir listeyi container record içine sarman gerekir.

---

## 5. İkisini Birlikte Kullanmak

İki düğme **farklı sorunları çözer ve doğal olarak birleşir**:
```java
.entity(ActorsFilms.class, spec -> spec
    .useProviderStructuredOutput()
    .validateSchema());
```
- `useProviderStructuredOutput()` → bozuk çıktı **ihtimalini en aza indirir** (önden kısıtlar).
- `validateSchema()` → geriye kalan uç durumları (provider edge case'leri, Ollama sorunu) **yakalar ve düzeltir**.

Downstream kodun **şekil kaymasına (shape drift) tahammül edemiyorsa** ikisini birden kullan.

---

## 6. Generic Tipler: `ParameterizedTypeReference`

- `.entity(Class)` sadece **somut sınıflar** içindir.
- `List<ActorsFilms>`, `Map<String, ActorsFilms>` gibi generic tipler için **`ParameterizedTypeReference`** kullanılır.
- Aynı `spec` consumer burada da çalışır.
- **Dikkat:** OpenAI'da `List<...>` + `useProviderStructuredOutput()` birleşimi top-level array yüzünden patlar. Çözüm tek satır: bir **wrapper record** (`record FilmographyList(List<ActorsFilms> films) {}`).

---

## 7. Tam Cevabı Almak: `.responseEntity(...)`

- `.entity(...)` sadece parse edilmiş nesneyi döner.
- Altta yatan `ChatResponse`'a da ihtiyacın varsa (token kullanımı, observability metadata vb.) → `.responseEntity(...)` kullan. Aynı overload'ları (Class, ParameterizedTypeReference, spec) destekler.

---

## 8. Built-in'ler Yetmediğinde: Custom Converter

- Built-in `BeanOutputConverter` **katıdır**: cevabın tamamen parse edilebilir JSON olmasını bekler.
- Ama modeller JSON'ı çoğu zaman **markdown code fence** (` ```json `) içine sarar veya başına "Here's the filmography:" gibi yazı ekler → converter ilk harfte hata fırlatır.
- Çözüm: Fence'leri temizleyip JSON'ı çıkaran, sonra varsayılan parser'a devreden bir **custom converter** (`LenientJsonOutputConverter`).

**`getJsonSchema()`'nın rolü (2.0'da eklendi):** Bu metot, bir converter'ın yeni iki düğmeye **katılmasını sağlayan köprüdür**.
- Onu uygularsan (genelde `BeanOutputConverter`'a delege ederek) → `validateSchema()` ve `useProviderStructuredOutput()` çalışır.
- Varsayılan halde bırakırsan → o converter için iki düğme de **no-op** (etkisiz) olur.

**JSON dışı formatlar (YAML, CSV):** `StructuredOutputConverter`'ı sıfırdan yaz, `getJsonSchema()`'yı varsayılan bırak → iki düğme devre dışı kalır, prompt tabanlı yol çalışır.

---

## Cheat Sheet

| İhtiyaç | Kullanılan |
|---|---|
| Varsayılan (her provider) | `.entity(Type.class)` |
| Generic tipler | `.entity(new ParameterizedTypeReference<...>(){})` |
| Bozuk çıktıda patlama | `.entity(..., spec -> spec.validateSchema())` |
| Provider'dan güçlü garanti | `.entity(..., spec -> spec.useProviderStructuredOutput())` |
| İkisi birden | `.useProviderStructuredOutput().validateSchema()` |
| Token/metadata da lazım | `.responseEntity(...)` |
| Fence/JSON-dışı format | Kendi `StructuredOutputConverter<T>`'unu yaz |
| Streaming | **Desteklenmiyor** — `.entity()` sadece `.call()` ile |

---

## En Önemli 3 Çıkarım

1. **`.entity(...)` tek başına garantisizdir** — model uyabilir de uymayabilir de. Güvenilirlik için ek önlem şart.
2. **İki tamamlayıcı yaklaşım var:** `validateSchema()` cevap tarafında düzeltir (retry + hatayı modele göstererek), `useProviderStructuredOutput()` istek tarafında zorlar. İkisi birlikte en güçlü kombinasyon.
3. **Geriye dönük uyumluluk korunmuş:** Varsayılanlar değişmedi, mevcut kod aynen çalışıyor, yeni özellikler **çağrı bazında opt-in**.