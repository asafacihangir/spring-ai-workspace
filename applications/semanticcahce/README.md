# Spring AI Semantic Caching

Bu proje, Spring AI ile geliştirilmiş küçük bir konsol sohbet uygulamasına
**semantic caching** desteği ekler.

Amaç, kullanıcı aynı soruyu farklı kelimelerle tekrar sorduğunda LLM'i yeniden
çağırmadan daha önce üretilmiş yanıtı cache'ten döndürmektir. Böylece hem yanıt
süresi kısalır hem de model kullanım maliyeti azalır.

## Problem

Klasik cache mekanizmaları genellikle metnin birebir aynı olmasına bakar.
Bu yüzden aşağıdaki iki soru aynı anlama gelse bile farklı istekler gibi ele alınır:

- `What is the height requirement for Big Thunder Mountain in Disneyland?`
- `How tall must I be to ride Big Thunder Mountain in Disneyland?`

Metinler farklıdır, ancak kullanıcının öğrenmek istediği bilgi aynıdır.
Semantic caching bu farkı yakalamak için soruları metin olarak değil,
anlam olarak karşılaştırır.

## Yaklaşım

Her kullanıcı sorusu için bir embedding üretilir ve Redis Stack üzerinde saklanır.
Yeni bir soru geldiğinde sistem şu adımları izler:

1. Sorunun embedding'i çıkarılır.
2. Redis Stack'teki mevcut embedding'lerle benzerlik araması yapılır.
3. Benzerlik değeri belirlenen eşiğin üzerindeyse cache'teki yanıt döndürülür.
4. Eşik aşılmazsa LLM çağrılır, üretilen yanıt sonraki benzer sorular için cache'e yazılır.

Bu sayede cache yalnızca birebir tekrarları değil, anlamca yakın soruları da yakalayabilir.

## Kullanılan Teknolojiler

| Katman | Teknoloji |
|--------|-----------|
| Dil / Çatı | Java 21, Spring Boot 4.0.7 |
| AI | Spring AI 2.0.0-RC2 |
| LLM / Embedding | OpenAI Chat `gpt-4o-mini` ve OpenAI Embedding |
| Cache / Vector Search | Redis Stack |
| Redis istemcisi | Jedis 7.4.1 |
| Çalıştırma | Maven, Docker, spring-boot-docker-compose |

Semantic arama için vector index ve vector search desteği gerektiğinden standart
Redis yerine Redis Stack kullanılır.

## Gereksinimler

- JDK 21+
- Docker
- Geçerli bir OpenAI API anahtarı

## Çalıştırma

Önce OpenAI API anahtarını ortam değişkeni olarak tanımlayın:

```bash
export OPENAI_API_KEY=sk-proj-...
```

Ardından uygulamayı başlatın:

```bash
./mvnw spring-boot:run
```

Redis Stack ayrıca kurulmak zorunda değildir. `spring-boot-docker-compose`,
`compose.yaml` dosyasındaki Redis Stack servisini otomatik olarak ayağa kaldırır.

## Örnek Kullanım

```text
Sohbet başladı. Çıkmak için 'exit' yazın.

> How tall must I be to ride Big Thunder Mountain in Disneyland?
... (1840 ms)

> What is the height requirement for Big Thunder Mountain?
... (12 ms)
```

İkinci soru farklı kelimelerle yazılmış olsa da ilk soruyla aynı anlama geldiği
için yanıt cache'ten döner.

## Başarı Kriteri

Bir soru LLM tarafından yanıtlandıktan sonra, aynı anlama gelen farklı bir soru
sorulduğunda sistemin LLM'i yeniden çağırmadan cache'teki yanıtı döndürmesi beklenir.
Bu durumda yanıt süresi belirgin biçimde düşer.
