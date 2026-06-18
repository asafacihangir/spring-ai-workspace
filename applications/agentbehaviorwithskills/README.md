# Agent Behavior with Skills

## Skills nedir?

AI ajanına belirli bir işi nasıl yapacağını öğreten talimat paketleridir.
Bir skill genellikle şunları içerir:

- Ne zaman kullanılacağı
- İşin adım adım nasıl yapılacağı
- Kurallar ve iyi uygulamalar
- Örnekler
- Gerekirse yardımcı script veya referans dosyaları

Örneğin bir `pdf-creator` skill'i, ajana PDF üretirken hangi araçları
kullanacağını, sayfa düzenini nasıl kontrol edeceğini ve çıktıyı nasıl
doğrulayacağını anlatabilir.

Bu projedeki skill (`src/main/resources/.agent/skills/weather/SKILL.md`):

```markdown
---
name: weather-assistant
description: Provides weather information and suggestions
---

When asked for the current weather for a location, use the
get-weather-for-zipcode tool. ... Follow the weather conditions with a
statement specific to the location:
- Miami, FL    → "Locals are in light jackets…"
- New York, NY → "Central Park is packed…"
```

## Nasıl çalışıyor?
```
Soru → agent ilgili skill'i yükler → skill "weather tool'unu çağır" der
     → WeatherTools veriyi döner → agent skill'deki anlatıma göre cevaplar
```



## Çalıştırma

```bash
# 1. OpenAI anahtarını ver
export OPENAI_API_KEY=sk-...

# 2. Uygulamayı başlat
./mvnw spring-boot:run

# 3. Sor (uygulama 8080'de)
curl -X POST localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What is the current weather in New York, NY?"}'
```

Beklenen cevap — yapılandırılmış veri **artı** konuma özel anlatı:

```
Current weather in New York, NY (zipcode 10001):
- Conditions: Sunny
- Temperature: 78F

Central Park is packed, iced coffees are mandatory, and someone's
loudly arguing about rent on the subway.
```


## Gereksinimler

- Java 21, Spring Boot 4.1, Spring AI 2.0
- Bir OpenAI API anahtarı
