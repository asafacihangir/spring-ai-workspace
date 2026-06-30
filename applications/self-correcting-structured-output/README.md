# Spring AI Self-Correcting Structured Output

Bu proje, Spring AI 2.0 ile LLM yanıtlarını tipli Java nesnelerine
(structured output) dönüştürmenin farklı yollarını ve bunların güvenilirliğini
artıran iki yeni mekanizmayı gösteren küçük bir demo uygulamasıdır.

> Öğrenme amaçlı demo. Kaynak blog:
> https://spring.io/blog/2026/06/23/spring-ai-self-correcting-structured-output

## Problem

LLM'ler metin-girer-metin-çıkar sistemlerdir; arayüzleri doğal dildir. Oysa
downstream kod bir alana göre dallanmak, bir değeri saklamak ya da sonucu tiplemek
istediğinde bu metnin bir kayda dönüşmesi gerekir. Structured output bu boşluğu
kapatır: model bir şemaya uyan metin üretmeye yönlendirilir, yanıt tekrar tipli
bir nesneye parse edilir.

Ancak model şemaya uymaya *zorlanmaz*, sadece *istenir*. Çoğu zaman uyar; bazen
fazladan alan ekler, zorunlu bir alanı atlar ya da JSON'u düz metinle (örneğin
markdown kod bloğuyla) sarar. Bu durumda parser patlar. Bu proje, parse'ı garanti
altına almanın katmanlı yollarını uçtan uca gösterir.

## Yaklaşım

Hedef tip basit bir Java record'udur (`ActorFilmography` → `Film`). Aynı tip için
beş ayrı endpoint, structured output'un farklı dialleriyle çalışır:

1. **plain** — baseline; yalnızca prompt'a şema eklenir, doğrulama yoktur.
2. **validated** — yanıt şemaya göre doğrulanır; hata varsa hata mesajı prompt'a
   eklenip çağrı tekrarlanır (kendi kendini düzelten döngü, varsayılan 3 deneme).
3. **provider** — şema API düzeyinde provider'a gönderilir; uyumsuz yanıt hiç
   üretilemez.
4. **lenient** — model JSON'u markdown kod bloğuna (```` ```json ````) sararsa
   bile çalışan özel converter (`LenientJsonOutputConverter`); fence'leri temizleyip
   JSON'u çıkarır, sonra varsayılan parser'a delege eder.
5. **full** — tipli nesneyle birlikte token kullanımı / metadata da döner.

## Kullanılan Teknolojiler

| Katman | Teknoloji |
|--------|-----------|
| Dil / Çatı | Java 21, Spring Boot 4.1.0 |
| AI | Spring AI 2.0.0 |
| LLM | OpenAI Chat |
| Structured Output | `BeanOutputConverter`, `StructuredOutputValidationAdvisor`, özel `StructuredOutputConverter` |
| Çalıştırma | Maven |

## Gereksinimler

- JDK 21+
- Maven (ya da proje içindeki `./mvnw` wrapper)
- Geçerli bir OpenAI API anahtarı → `OPENAI_API_KEY` ortam değişkeni

## Çalıştırma

Önce OpenAI API anahtarını ortam değişkeni olarak tanımlayın:

```bash
export OPENAI_API_KEY=sk-proj-...
```

Ardından uygulamayı başlatın:

```bash
./mvnw spring-boot:run
```

Uygulama varsayılan olarak `8080` portunda çalışır.

## Örnek Kullanım

İstek gövdesi tüm endpoint'lerde ortaktır:

```json
{ "prompt": "Tom Hanks için filmografi üret." }
```

### `POST /filmography/plain` — baseline (doğrulama yok)

```bash
curl -X POST localhost:8080/filmography/plain \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Tom Hanks için filmografi üret."}'
```

### `POST /filmography/validated` — self-correction döngüsü

```bash
curl -X POST localhost:8080/filmography/validated \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Tom Hanks için filmografi üret."}'
```

### `POST /filmography/provider` — API düzeyinde zorlama

```bash
curl -X POST localhost:8080/filmography/provider \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Tom Hanks için filmografi üret."}'
```

### `POST /filmography/lenient` — markdown kod bloğuna toleranslı converter

```bash
curl -X POST localhost:8080/filmography/lenient \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Tom Hanks için filmografi üret. Yanıtı ```json kod bloğu içine sar."}'
```

Aynı prompt `/plain` endpoint'inde parse hatası verir; `/lenient` ise fence'i
temizleyip tipli nesneyi döndürür.

### `POST /filmography/full` — token / metadata ile

```bash
curl -X POST localhost:8080/filmography/full \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Tom Hanks için filmografi üret."}'
```

Beklenen çıktı (tipli `ActorFilmography`):

```json
{
  "actor": "Tom Hanks",
  "birthYear": 1956,
  "films": [
    { "title": "Forrest Gump", "year": 1994 },
    { "title": "Cast Away", "year": 2000 }
  ]
}
```

`/full` ek olarak token kullanımı / metadata bilgisini döndürür.

## Başarı Kriteri

Model şemaya uymayan ya da JSON'u düz metinle saran bir yanıt ürettiğinde,
`validated` self-correction'la, `provider` API düzeyinde zorlamayla ve `lenient`
toleranslı parse'la geçerli ve tipli bir `ActorFilmography` döndürebilmelidir;
oysa `plain` aynı durumda parse hatası verir.
