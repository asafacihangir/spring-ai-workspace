# SkillsJar

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

## SkillsJar nedir?

SkillsJar, AI agent skill'lerinin standart bir Java `.jar` dosyası içinde
paketlenmesidir.

Normalde bir skill klasör olarak bulunur:

```
code-review/
├── SKILL.md
├── references/
└── scripts/
```

SkillsJar bunu bir JAR içine koyar:

```
my-company-skills.jar
└── META-INF/
    └── skills/
        └── my-company/
            └── code-review/
                ├── SKILL.md
                ├── references/
                └── scripts/
```

Agent veya framework, JAR'ı classpath üzerinden bulur ve içindeki skill'leri
yükler.

Örneğin:

```xml
<dependency>
    <groupId>com.skillsjars</groupId>
    <artifactId>anthropics__skills__pdf</artifactId>
    <version>2026_02_25-3d59511</version>
</dependency>
```

Bu bağımlılık eklendiğinde AI agent, JAR içindeki PDF skill'ine erişebilir. Bu
skill, agent'a örneğin şu konularda çalışma talimatları sağlar:

- PDF oluşturma
- PDF içeriğini okuma ve analiz etme
- PDF birleştirme veya bölme
- Form doldurma
- Sayfa düzenleme
- OCR uygulama
- Üretilen PDF'i doğrulama

## Maven ne yapar?

Dependency eklendiğinde Maven:

1. JAR'ı repository'den indirir.
2. Uygulamanın classpath'ine ekler.
3. Skill yükleyicisinin JAR içindeki dosyalara erişmesini sağlar.

Ancak yalnızca dependency eklemek çoğu sistemde yeterli değildir. Agent
framework'üne skill'leri nereden okuyacağını da söylemek gerekir. Örneğin:

```properties
agent.skills.paths=classpath:/META-INF/skills
```

Böylece framework, dependency JAR'larının içindeki `META-INF/skills`
klasörlerini tarar.

## Uygulama nasıl çalışıyor

```
POST /stories  →  hikaye yaz  →  pdf skill'ini yükle  →  scriptini çalıştır  →  PDF
```

Agent üç tool'a sahip (bkz. `ChatClientConfig`):

- **SkillsTool** — JAR'daki skill'leri yükler
- **ShellTools** — skill'in söylediği komutları/Python scriptlerini çalıştırır
- **FileSystemTools** — dosya okur/yazar

Akış: model `pdf` skill'ini yükler, talimatlarını okur, gereken scripti
shell'den çalıştırır ve PDF'i istenen mutlak yola yazar.

## Çalıştır

```bash
export OPENAI_API_KEY=sk-...
./mvnw spring-boot:run
```

İstek at:

```bash
curl -X POST localhost:8080/stories \
  -H 'Content-Type: application/json' \
  -d '{"topic": "a lighthouse keeper and a whale"}'
```

Dönen cevap PDF'in yolunu ve kısa bir özeti içerir. PDF varsayılan olarak
`./output/` altına yazılır (`app.output-dir`).

## Gereksinimler

- Java 21, Spring Boot 4 / Spring AI 2
- PDF skill'i Python scriptlerine dayanır → makinede `python3` kurulu olmalı.