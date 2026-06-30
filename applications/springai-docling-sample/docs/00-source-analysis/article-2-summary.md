# Article 2: Retrieval Augmented Generation with Docling, Java and Spring AI

## Metadata

| Field | Value |
|-------|-------|
| **Title** | Retrieval Augmented Generation with Docling, Java and Spring AI |
| **Author** | Thomas Vitale |
| **URL** | https://www.thomasvitale.com/rag-docling-java-spring-ai/ |
| **Publication Date** | December 15, 2025 |
| **Reading Time** | 8 minutes |

---

## Key Concepts

### 1. Retrieval Augmented Generation (RAG)
LLM'lerin sadece training data'ya değil, kendi verilerinize dayalı sorulara yanıt vermesini sağlayan teknik. External knowledge source'ları dinamik olarak generation process'ine dahil eder.

### 2. Ingestion Pipeline (ETL)
Üç aşamalı veri işleme süreci:

| Stage | Component | Responsibility |
|-------|-----------|----------------|
| **Extract** | DocumentReader | Çeşitli kaynaklardan veri yükleme ve parse etme |
| **Transform** | DocumentTransformer | RAG flow'ları için uygun hale getirme |
| **Load** | DocumentWriter | Vector database'e depolama |

### 3. Privacy & On-Premises
Tüm sistem on-premises çalışır:
- Cloud bağımlılığı yok
- Harici servislere veri gönderimi yok
- Tam veri gizliliği sağlanır

---

## Technical Components

### Core Dependencies

| Library | Purpose |
|---------|---------|
| `io.arconia:arconia-ai-docling-document-reader` | Docling ile document reading |
| `org.springframework.ai:spring-ai-starter-model-ollama` | Local LLM inference |
| `org.springframework.ai:spring-ai-starter-vector-store-pgvector` | Vector database |
| `org.springframework.ai:spring-ai-rag` | RAG framework |

### AI Models

| Model | Type | Purpose |
|-------|------|---------|
| `granite4:3b` | Chat Model | Question answering |
| `granite-embedding:278m` | Embedding Model | 768-dimensional vectors |

### Infrastructure

| Component | Technology |
|-----------|------------|
| Application Framework | Spring Boot 3.5 |
| Build Tool | Gradle |
| Language | Java 25 |
| Local LLM Service | Ollama |
| Vector Database | PostgreSQL + PGVector |
| Container Management | Testcontainers |

### Version Information
- **Arconia**: 0.20.0
- **Spring AI**: 1.1.2
- **Spring Boot**: 3.5
- **Java**: 25

---

## Code Snippets

### 1. Gradle Dependencies
```gradle
dependencies {
    implementation 'io.arconia:arconia-ai-docling-document-reader'
    implementation 'org.springframework.ai:spring-ai-starter-model-ollama'
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-pgvector'
    implementation 'org.springframework.ai:spring-ai-rag'
    implementation 'org.springframework.boot:spring-boot-starter-web'

    testAndDevelopmentOnly 'io.arconia:arconia-dev-services-docling'
    testAndDevelopmentOnly 'io.arconia:arconia-dev-services-ollama'
    testAndDevelopmentOnly 'io.arconia:arconia-dev-services-postgresql'
}

dependencyManagement {
    imports {
        mavenBom "io.arconia:arconia-bom:0.20.0"
        mavenBom "org.springframework.ai:spring-ai-bom:1.1.2"
    }
}
```

### 2. Ollama Model Configuration
```yaml
spring:
  ai:
    ollama:
      init:
        pull-model-strategy: when-missing
      chat:
        model: granite4:3b
      embedding:
        model: granite-embedding:278m
```

### 3. PGVector Configuration
```yaml
spring:
  ai:
    vectorstore:
      pgvector:
        dimensions: 768
        index-type: hnsw
        initialize-schema: true

arconia:
  dev:
    services:
      postgresql:
        image-name: pgvector/pgvector:pg18
```
**Note:** `dimensions: 768` embedding model ile eşleşmeli. `hnsw` (Hierarchical Navigable Small World) semantic search için kullanılır.

### 4. Ingestion Pipeline Component
```java
@Component
class IngestionPipeline {
    private final DoclingServeApi doclingServeApi;
    private final VectorStore vectorStore;

    IngestionPipeline(DoclingServeApi doclingServeApi, VectorStore vectorStore) {
        this.doclingServeApi = doclingServeApi;
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    void run() {
        Resource file = new ClassPathResource("documents/story.pdf");

        List<Document> documents = DoclingDocumentReader.builder()
                .doclingServeApi(doclingServeApi)
                .files(file)
                .build()
                .get();

        vectorStore.add(documents);
    }
}
```
**Note:** `@PostConstruct` ile application startup'ta çalışır. DoclingDocumentReader hierarchical strategies ile otomatik chunking yapar.

### 5. RAG Chat Controller
```java
@RestController
class ChatController {
    private final ChatClient chatClient;

    ChatController(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder
            .defaultAdvisors(RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                    .vectorStore(vectorStore)
                    .build())
                .build())
            .build();
    }

    @GetMapping("/chat")
    String chat(String question) {
        return chatClient
                .prompt(question)
                .call()
                .content();
    }
}
```
**Note:** `RetrievalAugmentationAdvisor` her request'i intercept eder ve relevant context ekler.

---

## Architecture Details

### Ingestion Pipeline Flow
```
PDF/Documents → DoclingDocumentReader → Chunking → Embeddings → PGVector
                     ↓
              Docling Serve (parsing, layout analysis, table recognition)
```

### RAG Query Flow
```
User Question → ChatController → RetrievalAugmentationAdvisor
                                         ↓
                              VectorStoreDocumentRetriever
                                         ↓
                              Semantic Search (PGVector)
                                         ↓
                              Relevant Documents Retrieved
                                         ↓
                              Augmented Prompt → Granite4 Model
                                         ↓
                              Contextual Response → User
```

### Component Integration
```
┌─────────────────────────────────────────────────────────────────┐
│                        Spring Boot Application                   │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────┐ │
│  │ IngestionPipeline│    │  ChatController │    │   Health    │ │
│  │    (@Component) │    │ (@RestController)│    │  Actuator   │ │
│  └────────┬────────┘    └────────┬────────┘    └─────────────┘ │
│           │                      │                              │
│           ▼                      ▼                              │
│  ┌─────────────────┐    ┌─────────────────┐                    │
│  │DoclingDocumentReader│ │   ChatClient    │                    │
│  │    (Spring AI)  │    │   (Spring AI)   │                    │
│  └────────┬────────┘    └────────┬────────┘                    │
│           │                      │                              │
├───────────┼──────────────────────┼──────────────────────────────┤
│           ▼                      ▼                              │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────┐ │
│  │  Docling Serve  │    │     Ollama      │    │  PGVector   │ │
│  │   (Container)   │    │   (Container)   │    │ (Container) │ │
│  └─────────────────┘    └─────────────────┘    └─────────────┘ │
│          Dev Services (Testcontainers)                          │
└─────────────────────────────────────────────────────────────────┘
```

---

## Implementation Steps

### Step 1: Project Setup
- Spring Boot 3.5 projesi oluştur
- Gradle build tool kullan

### Step 2: Dependencies
- Arconia BOM 0.20.0 ekle
- Spring AI BOM 1.1.2 ekle
- Tüm required dependencies ekle

### Step 3: Ollama Setup
- Ollama'yı local olarak install et ve çalıştır
- granite4:3b ve granite-embedding:278m modellerini configure et

### Step 4: Vector Store Configuration
- PGVector 768-dimensional vectors için configure et
- HNSW index type seç
- Schema initialization aktif et

### Step 5: Ingestion Pipeline
- `IngestionPipeline` component oluştur
- `@PostConstruct` ile startup'ta çalıştır
- DoclingDocumentReader ile PDF'leri işle
- VectorStore'a embeddings kaydet

### Step 6: RAG Controller
- `ChatController` oluştur
- `RetrievalAugmentationAdvisor` configure et
- `/chat` endpoint tanımla

### Step 7: Run Application
```bash
# Start application (Dev Services auto-provision containers)
./gradlew bootRun

# Or with Arconia CLI
arconia dev
```

### Step 8: Test RAG
```bash
http :8080/chat question=="What's Iorek's dream?"
```

**Expected Response:**
> "Iorek dreams of one day going on an adventure around the North Pole and seeing the Northern Lights."

---

## Key Takeaways

1. **Privacy-First**: Tüm processing local, cloud bağımlılığı yok
2. **Automatic Infrastructure**: Dev Services otomatik container provisioning
3. **Hierarchical Chunking**: Docling intelligent document chunking sağlar
4. **Semantic Search**: HNSW index ile efficient similarity search
5. **Advisor Pattern**: Spring AI advisor'ları ile clean RAG implementation

---

## Implementation Decision Note

> **NOT (2025-12-24):** Bu projede LLM inference için makaledeki Ollama yerine **OpenAI GPT-4o** kullanılmasına karar verilmiştir.
>
> **Değişiklikler:**
> - Chat Model: `granite4:3b` → `gpt-4o` (OpenAI)
> - Embedding Model: `granite-embedding:278m` (768d) → `text-embedding-3-small` (1536d)
> - Vector Store: `dimensions: 768` → `dimensions: 1536`
> - Dependency: `spring-ai-starter-model-ollama` → `spring-ai-starter-model-openai`
> - Dev Services: `arconia-dev-services-ollama` kaldırıldı
>
> Makaledeki Docling entegrasyonu ve RAG pattern'ı aynen kullanılacaktır.