# Article 1: AI Document Processing with Docling, Java, Arconia and Spring Boot

## Metadata

| Field | Value |
|-------|-------|
| **Title** | AI Document Processing with Docling Java, Arconia, and Spring Boot |
| **Author** | Thomas Vitale |
| **URL** | https://www.thomasvitale.com/ai-document-processing-docling-java-arconia-spring-boot/ |
| **Publication Date** | November 23, 2025 |
| **Last Modified** | November 24, 2025 |
| **Reading Time** | 12 minutes |

---

## Key Concepts

### 1. Core Problem Statement
Document processing for Generative AI uygulamaları üç temel zorlukla karşı karşıya:
- **Resource Intensity**: GenAI çözümleri yoğun hesaplama kaynakları gerektirir
- **Privacy Concerns**: Cloud-based SaaS modelleri veri güvenliği sorunları yaratır
- **Hallucination Risk**: Language model'ler kaynak dokümanlarda olmayan içerik üretebilir

### 2. Docling Nedir?
IBM Research Zurich'in "AI for knowledge" ekibi tarafından başlatılan, Linux Foundation AI & Data'ya bağışlanmış açık kaynak çözüm.

**Temel Özellikler:**
- MIT License ile dağıtılır (permissive, open-source)
- PDF, DOCX ve presentation dosyalarını temiz, yapılandırılmış veriye dönüştürür
- RAG, GenAI uygulamaları için hazır çıktı üretir

### 3. Docling'in Sağladığı Çözümler

| Problem | Çözüm |
|---------|-------|
| Efficiency | Commodity hardware üzerinde çalışır, local ve edge device destekler |
| Privacy | Air-gapped ortamlarda tam veri kontrolü sağlar |
| Fidelity | Özelleşmiş ML modelleri ile halüsinasyonsuz dönüşüm |

### 4. Teknik Özellikler
- **Extensibility**: Custom document processing pipeline'ları destekler
- **Model Flexibility**: VLM (Granite Docling) ve ASR modelleri ile uyumlu
- **Unified Format**: `DoclingDocument` formatı metadata kaybı olmadan tüm input'ları temsil eder
- **API Deployment**: Docling Serve projesi ile API servisi olarak çalışır

---

## Technical Components

### Docling Java Project Structure

| Module | Description |
|--------|-------------|
| **Docling Serve API** | Docling Serve backend ile iletişim için core API'ler |
| **Docling Serve Client** | Java HttpClient kullanan referans HTTP client implementasyonu |
| **Docling Testcontainers** | Development/testing için `DoclingServeContainer` sağlar |

### Spring Boot Integration Stack

| Component | Purpose |
|-----------|---------|
| `arconia-docling-spring-boot-starter` | Spring Boot auto-configuration |
| `arconia-dev-services-docling` | Local Docling Serve instance provisioning |
| Spring Framework RestClient | HTTP interactions |
| Spring Boot Actuator | Health monitoring |
| Testcontainers | Container runtime management |
| OpenTelemetry | Observability and tracing |
| Micrometer | Metrics standardization |

### Version Information
- **Arconia**: 0.19.0
- **Spring Boot**: 3.5
- **Java**: 25
- **Build Tool**: Gradle

---

## Code Snippets

### 1. Gradle Dependencies
```gradle
dependencies {
  implementation 'org.springframework.boot:spring-boot-starter-actuator'
  implementation 'org.springframework.boot:spring-boot-starter-web'
  implementation 'io.arconia:arconia-docling-spring-boot-starter'
  testAndDevelopmentOnly 'io.arconia:arconia-dev-services-docling'
}

dependencyManagement {
  imports {
    mavenBom "io.arconia:arconia-bom:0.19.0"
  }
}
```

### 2. Document Processing Controller (HTTP Source)
```java
@RestController
@RequestMapping("/convert")
class DocumentProcessingController {

  private final DoclingServeApi doclingServeApi;

  DocumentProcessingController(DoclingServeApi doclingServeApi) {
    this.doclingServeApi = doclingServeApi;
  }

  @GetMapping("/http")
  String convertDocumentFromHttp(@RequestParam("url") String url) {
    ConvertDocumentResponse response = doclingServeApi
      .convertSource(ConvertDocumentRequest.builder()
        .source(HttpSource.builder().url(URI.create(url)).build())
        .build());
    return response.getDocument().getMarkdownContent();
  }
}
```

### 3. Document Processing Controller (File Source)
```java
@GetMapping("/file")
String convertDocumentFromFile() throws IOException {
  Resource file = new ClassPathResource("documents/story.pdf");
  String base64File = Base64.getEncoder().encodeToString(file.getContentAsByteArray());
  ConvertDocumentResponse response = doclingServeApi
    .convertSource(ConvertDocumentRequest.builder()
      .source(FileSource.builder()
        .filename("story.pdf")
        .base64String(base64File)
        .build())
      .build());
  return response.getDocument().getMarkdownContent();
}
```

### 4. Application Configuration
```yaml
management:
  endpoint:
    health:
      show-components: always
```

### 5. JBang Standalone Application
```java
//usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25
//DEPS io.arconia:arconia-docling-spring-boot-starter:0.19.0
//DEPS io.arconia:arconia-dev-services-docling:0.19.0

package io.arconia.docling;

import ai.docling.api.serve.DoclingServeApi;
import ai.docling.api.serve.convert.request.*;
import ai.docling.api.serve.convert.request.source.HttpSource;
import java.net.URI;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
    void main(String[] args) {
        var application = new SpringApplication(Application.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }

    @Bean
    CommandLineRunner convert(DoclingServeApi doclingServeApi) {
        return _ -> {
            var url = "https://docs.arconia.io/arconia/latest/integrations/docling";
            var response = doclingServeApi.convertSource(
                ConvertDocumentRequest.builder()
                    .source(HttpSource.builder()
                        .url(URI.create(url))
                        .build())
                    .build());
            IO.println(response.getDocument().getMarkdownContent());
        };
    }
}
```

---

## Architecture Details

### Docling Processing Pipeline

**ML Model Foundation:**
- Layout analysis (document structure understanding)
- Table recognition (tabular data extraction)
- OCR (image to text conversion)

**Unified Output Model:**
- Tüm input'lar `DoclingDocument` formatına dönüştürülür
- Metadata kaybı olmaz (Markdown/HTML'den farklı olarak)

### Docling Serve Architecture
```
Python Package/CLI → Docling Serve (HTTP API) → Java Client → Spring Boot App
```

### Spring Boot Integration Architecture

**Auto-Configuration Flow:**
1. `arconia-docling-spring-boot-starter` dependency eklenir
2. Spring Boot otomatik olarak `DoclingServeApi` bean'i configure eder
3. Dev Service containerized Docling Serve başlatır
4. Application bean'leri `DoclingServeApi`'yi autowire eder

**Dev Services Mechanism:**
- Testcontainers Docker/Podman container'ları yönetir
- Development sırasında otomatik infrastructure provision
- Workspace'teki birden fazla uygulama aynı instance'ı paylaşır
- Production build'lerinden tamamen excluded

---

## Implementation Steps

### Phase 1: Project Setup
1. Spring Boot projesi oluştur (Spring Initializr veya manual)
2. Dependencies seç: Spring Web, Actuator
3. Spring Boot 3.5+, Gradle, Java 25 kullan

### Phase 2: Dependency Configuration
1. `arconia-docling-spring-boot-starter` ekle (production)
2. `arconia-dev-services-docling` ekle (`testAndDevelopmentOnly` scope)
3. BOM version consistency sağla

### Phase 3: Controller Implementation
1. `@RestController` class oluştur
2. `DoclingServeApi` constructor injection
3. Endpoint method'ları tanımla
4. `ConvertDocumentRequest` build et
5. `doclingServeApi.convertSource(request)` çağır

### Phase 4: Application Execution
```bash
# Standard Spring Boot
./gradlew bootRun

# Arconia CLI
arconia dev

# JBang
jbang Application.java
```

### Phase 5: Health Monitoring
- `/actuator/health` endpoint'i erişilebilir
- `"docling": {"status": "UP"}` verify et

### Phase 6: Development Tools
- Docling Serve UI: `http://localhost:<port>/ui`
- API Documentation: `http://localhost:<port>/docs`

---

## Future Roadmap (Makale'den)
- Java SDK'da genişletilmiş yetenekler
- Spring AI data ingestion pipeline API'leri ile entegrasyon
- RAG workflows ve agentic applications için geliştirilmiş destek
- Standard HTTP observability ötesinde ek instrumentation

---

## Implementation Decision Note

> **NOT (2025-12-24):** Bu projede LLM inference için Ollama yerine **OpenAI GPT-4o** kullanılmasına karar verilmiştir.
> - Chat Model: `gpt-4o` (OpenAI)
> - Embedding Model: `text-embedding-3-small` (1536 dimensions)
> - Makaledeki Docling entegrasyonu aynen kullanılacak, sadece AI model provider değişmiştir.