# Technology Stack & Architecture Decisions

Bu doküman, Spring AI + Docling entegrasyonu projesinin teknoloji stack'ini ve mimari kararlarını içermektedir.

---

## 1. Core Technologies

### 1.1 Application Framework

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 25 | Programming language |
| **Spring Boot** | 3.5+ | Application framework |
| **Spring AI** | 1.1.2+ | AI/ML integration framework |
| **Maven** | 3.9+ | Build tool |

### 1.2 Document Processing

| Technology | Version | Purpose |
|------------|---------|---------|
| **Docling** | Latest | Document parsing (IBM Research) |
| **Docling Serve** | Latest | HTTP API wrapper for Docling |
| **Arconia** | 0.20.0+ | Spring Boot integration for Docling |

### 1.3 AI/ML Infrastructure

| Technology | Version | Purpose |
|------------|---------|---------|
| **OpenAI API** | Latest | Cloud LLM inference platform |
| **GPT-4o** | Latest | Chat model for Q&A |
| **text-embedding-3-small** | Latest | Embedding model (1536 dimensions) |

### 1.4 Data Storage

| Technology | Version | Purpose |
|------------|---------|---------|
| **PostgreSQL** | 18 | Relational database |
| **PGVector** | Latest | Vector storage extension |

### 1.5 Development & Testing

| Technology | Version | Purpose |
|------------|---------|---------|
| **Testcontainers** | Latest | Container management for dev/test |
| **JUnit 5** | Latest | Testing framework |
| **Docker/Podman** | Latest | Container runtime |

### 1.6 Observability

| Technology | Version | Purpose |
|------------|---------|---------|
| **Spring Boot Actuator** | 3.5+ | Health & metrics endpoints |
| **Micrometer** | Latest | Metrics collection |
| **OpenTelemetry** | Latest | Distributed tracing |
| **Grafana** | Latest | Visualization (optional) |

---

## 2. Architecture Style

### 2.1 Feature-Based Layered Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Spring Boot Application                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                            FEATURES                                    │  │
│  │                                                                        │  │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐       │  │
│  │  │    document     │  │      chat       │  │     health      │       │  │
│  │  │  ─────────────  │  │  ─────────────  │  │  ─────────────  │       │  │
│  │  │  api/           │  │  api/           │  │  api/           │       │  │
│  │  │  application/   │  │  application/   │  │  application/   │       │  │
│  │  │  domain/        │  │  domain/        │  │  domain/        │       │  │
│  │  │  infrastructure/│  │  infrastructure/│  │  infrastructure/│       │  │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘       │  │
│  │           │                    │                    │                 │  │
│  │           └────────────────────┴────────────────────┘                 │  │
│  │                                │                                       │  │
│  │                   ┌────────────▼────────────┐                         │  │
│  │                   │        shared           │                         │  │
│  │                   │  domain/ infrastructure/│                         │  │
│  │                   └─────────────────────────┘                         │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                    ┌─────────────────┼─────────────────┐
                    │                 │                 │
                    ▼                 ▼                 ▼
           ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
           │ Docling Serve│  │  OpenAI API  │  │   PGVector   │
           │  (Container) │  │   (Cloud)    │  │  (Container) │
           └──────────────┘  └──────────────┘  └──────────────┘
```

### 2.2 Feature Layer Structure

Her feature kendi içinde 4 katmana sahiptir:

| Layer | Location | Responsibility |
|-------|----------|---------------|
| **API** | `{feature}/api/` | Controllers, DTOs, request/response handling |
| **Application** | `{feature}/application/` | Use cases, ports (interfaces) |
| **Domain** | `{feature}/domain/` | Entities, Value Objects, Domain Events (pure Java) |
| **Infrastructure** | `{feature}/infrastructure/` | Adapters, external integrations |

### 2.3 Key Patterns

| Pattern | Usage |
|---------|-------|
| **Feature-Based Organization** | Code organized by business capability |
| **Ports & Adapters** | Each feature has its own ports and adapters |
| **Dependency Injection** | Spring IoC container for bean management |
| **Adapter Pattern** | External API integration (Docling, OpenAI) |
| **Domain Events** | Loose coupling between features |

---

## 3. Package Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/example/docling/
│   │       ├── DoclingApplication.java
│   │       │
│   │       ├── shared/                      # Shared kernel (minimal!)
│   │       │   ├── domain/
│   │       │   │   └── DomainEvent.java
│   │       │   └── infrastructure/
│   │       │       └── SpringEventPublisher.java
│   │       │
│   │       ├── document/                    # Document Processing Feature
│   │       │   ├── api/
│   │       │   │   ├── DocumentController.java
│   │       │   │   └── dto/
│   │       │   ├── application/
│   │       │   │   ├── ConvertDocumentUseCase.java
│   │       │   │   └── port/
│   │       │   ├── domain/
│   │       │   │   ├── Document.java
│   │       │   │   ├── DocumentId.java
│   │       │   │   └── event/
│   │       │   └── infrastructure/
│   │       │       └── DoclingServeAdapter.java
│   │       │
│   │       ├── chat/                        # RAG Chat Feature
│   │       │   ├── api/
│   │       │   │   ├── ChatController.java
│   │       │   │   └── dto/
│   │       │   ├── application/
│   │       │   │   ├── ProcessQueryUseCase.java
│   │       │   │   └── port/
│   │       │   ├── domain/
│   │       │   │   ├── Query.java
│   │       │   │   ├── DocumentChunk.java
│   │       │   │   └── event/
│   │       │   └── infrastructure/
│   │       │       ├── OpenAIEmbeddingAdapter.java
│   │       │       ├── OpenAIChatAdapter.java
│   │       │       └── PgVectorStoreAdapter.java
│   │       │
│   │       ├── health/                      # Health Monitoring Feature
│   │       │   ├── api/
│   │       │   │   └── HealthController.java
│   │       │   ├── application/
│   │       │   │   └── CheckHealthUseCase.java
│   │       │   └── infrastructure/
│   │       │       └── *HealthIndicator.java
│   │       │
│   │       └── config/                      # Application-wide configuration
│   │           ├── ApplicationConfig.java
│   │           └── IngestionPipeline.java
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── documents/
│
└── test/
    └── java/
        └── com/example/docling/
            ├── document/
            │   ├── domain/
            │   └── application/
            ├── chat/
            │   ├── domain/
            │   └── application/
            └── health/
```

---

## 4. Configuration

### 4.1 Application Configuration (application.yml)

```yaml
spring:
  application:
    name: springai-docling-sample

  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        model: gpt-4o
      embedding:
        model: text-embedding-3-small
    vectorstore:
      pgvector:
        dimensions: 1536
        index-type: hnsw
        initialize-schema: true

management:
  endpoint:
    health:
      show-components: always
  endpoints:
    web:
      exposure:
        include: health,info,metrics

arconia:
  dev:
    services:
      postgresql:
        image-name: pgvector/pgvector:pg18
```

### 4.2 Development Configuration (application-dev.yml)

```yaml
# Dev Services otomatik olarak container'ları başlatır
# Ek konfigürasyon gerekmez
logging:
  level:
    com.example.docling: DEBUG
    ai.docling: DEBUG
```

### 4.3 Production Configuration (application-prod.yml)

```yaml
arconia:
  docling:
    url: ${DOCLING_SERVE_URL}

spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
```

---

## 5. Dependencies (Maven)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.0</version>
    </parent>

    <properties>
        <java.version>25</java.version>
        <spring-ai.version>1.1.2</spring-ai.version>
        <arconia.version>0.20.0</arconia.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>${spring-ai.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>io.arconia</groupId>
                <artifactId>arconia-bom</artifactId>
                <version>${arconia.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Spring AI -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-openai</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-vector-store-pgvector</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-rag</artifactId>
        </dependency>

        <!-- Arconia Docling -->
        <dependency>
            <groupId>io.arconia</groupId>
            <artifactId>arconia-ai-docling-document-reader</artifactId>
        </dependency>

        <!-- Dev Services (test scope) -->
        <dependency>
            <groupId>io.arconia</groupId>
            <artifactId>arconia-dev-services-docling</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.arconia</groupId>
            <artifactId>arconia-dev-services-postgresql</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

---

## 6. Architecture Decision Records (ADRs)

### ADR-001: Use Docling for Document Processing

**Context:** Need privacy-focused, on-premises document processing capability.

**Decision:** Use Docling (IBM Research) via Arconia Spring Boot integration.

**Rationale:**
- MIT License, open-source
- On-premises processing, no cloud dependency
- Specialized ML models for layout analysis, table extraction, OCR
- Spring Boot auto-configuration support

**Consequences:**
- Container runtime (Docker/Podman) required
- Additional infrastructure component to manage

---

### ADR-002: Use OpenAI for LLM Inference

**Context:** Need high-quality LLM inference for document Q&A.

**Decision:** Use OpenAI API with GPT-4o and text-embedding-3-small models.

**Rationale:**
- State-of-the-art model quality (GPT-4o)
- Excellent embedding quality (1536 dimensions)
- No local infrastructure required
- Consistent and reliable API
- Fast inference without local GPU requirements

**Consequences:**
- API costs per request
- Requires internet connectivity
- Data sent to OpenAI servers
- API key management required

---

### ADR-003: Use PGVector for Vector Storage

**Context:** Need efficient vector storage with semantic search capability.

**Decision:** Use PostgreSQL with PGVector extension.

**Rationale:**
- Familiar PostgreSQL ecosystem
- HNSW index for fast approximate nearest neighbor search
- Good integration with Spring AI
- Production-ready and scalable

**Consequences:**
- Additional PostgreSQL extension management
- Vector dimension must match embedding model (1536)

---

### ADR-004: Use Spring AI Advisor Pattern for RAG

**Context:** Need clean abstraction for RAG implementation.

**Decision:** Use RetrievalAugmentationAdvisor with ChatClient.

**Rationale:**
- Declarative RAG configuration
- Automatic context retrieval and prompt augmentation
- Clean separation of concerns
- Consistent with Spring AI patterns

**Consequences:**
- Learning curve for advisor pattern
- Limited customization without custom advisor

---

## 7. Deployment Options

### 7.1 Development

```bash
# Start with Dev Services (auto-provisions containers)
mvn spring-boot:run

# Or with Arconia CLI
arconia dev
```

### 7.2 Production (Docker Compose)

```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DOCLING_SERVE_URL=http://docling:8001
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - DATABASE_URL=jdbc:postgresql://postgres:5432/docling
    depends_on:
      - docling
      - postgres

  docling:
    image: quay.io/docling/docling-serve:latest
    ports:
      - "8001:8001"

  postgres:
    image: pgvector/pgvector:pg18
    environment:
      - POSTGRES_DB=docling
      - POSTGRES_USER=docling
      - POSTGRES_PASSWORD=docling
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

### 7.3 Kubernetes

Kubernetes deployment için `helm-chart-scaffolding` skill kullanılabilir.

---

## Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-23 | AI Assistant | Initial technology stack documentation |
| 1.1 | 2025-12-24 | AI Assistant | Switched from Ollama to OpenAI GPT-4o |
| 2.0 | 2025-12-24 | AI Assistant | Migrated to Feature-Based Layered Architecture, Gradle to Maven |