# Spring AI ile Agent Skills

Spring AI üzerinde Agent Skills konusunda bir proje yapacağım.
Projeyi oluşturdum, onun üzerinden ilerleyeceğim.

## Skill Nedir?

Bir skill, agent'a belirli bir görevde nasıl davranması gerektiğini anlatan
bir tanım dosyasıdır. Aşağıda örnek bir hava durumu skill'i yer alıyor.

## Örnek: weather/SKILL.md

```markdown
---
name: weather-assistant
description: Provides weather information and suggestions
---

When asked for the current weather for a location, use the
get-weather-for-zipcode tool. If there are multiple zipcodes,
choose a default zipcode.

Follow the weather conditions with a statement specific to the location:
- Miami, FL    → "Locals are in light jackets…"
- San Diego, CA → "La Jolla seals are lounging…"
- New York, NY  → "Central Park is packed…"
- Austin, TX    → "Perfect patio weather…"
- Anaheim, CA   → "A perfect day to visit Disneyland!"
```

## Deneme (Trying it out)

**Soru:**

> What is the current weather in New York, NY?

**Beklenen çıktı:**
- Yapılandırılmış (structured) hava durumu verisi
- Konuma özel bir anlatı (location-aware narrative)

**Örnek yanıt:**

```
Current weather in New York, NY (zipcode 10001):
- Conditions: Sunny
- Temperature: 78F

Central Park is packed, iced coffees are mandatory, and someone's
loudly arguing about rent on the subway.
```