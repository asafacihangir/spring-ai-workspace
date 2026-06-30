        # Business Requirements - Spring AI + Docling Integration

## 1. Project Overview

### 1.1 Proje Tanımı
Bu proje, Spring AI ve Docling teknolojilerini kullanarak kurumsal doküman işleme ve Retrieval Augmented Generation (RAG) yetenekleri sunan bir uygulama geliştirmeyi amaçlamaktadır.

### 1.2 Problem Statement
Generative AI uygulamalarında doküman işleme üç temel zorlukla karşı karşıyadır:

| Problem | Açıklama |
|---------|----------|
| **Resource Intensity** | GenAI çözümleri yoğun hesaplama kaynakları gerektirir, on-premises deployment'ı zorlaştırır |
| **Privacy Concerns** | Cloud-based SaaS modelleri veri güvenliği ve gizlilik sorunları yaratır |
| **Hallucination Risk** | Language model'ler kaynak dokümanlarda olmayan içerik üretebilir (halüsinasyon) |

### 1.3 Proposed Solution
Docling (IBM Research), Spring AI ve OpenAI GPT-4o entegrasyonu ile:
- On-premises doküman işleme (Docling ile)
- Yüksek kaliteli LLM inference (OpenAI GPT-4o ile)
- Açık kaynak doküman processing çözümü (MIT lisanslı Docling)
- Özelleşmiş ML modelleri ile halüsinasyonsuz dönüşüm
- RAG tabanlı soru-cevap sistemi

---

## 2. Business Goals

### 2.1 Primary Goals

| ID | Goal | Success Criteria |
|----|------|------------------|
| BG-01 | On-premises doküman işleme yeteneği sağlamak | Tüm doküman işleme local environment'ta gerçekleşmeli |
| BG-02 | Doküman işleme gizliliğini korumak | Doküman parsing on-premises gerçekleşmeli |
| BG-03 | RAG tabanlı bilgi erişimi sunmak | Kullanıcılar dokümanlar hakkında doğal dilde soru sorabilmeli |
| BG-04 | Çoklu doküman formatı desteği | PDF, DOCX, presentation dosyaları işlenebilmeli |

### 2.2 Secondary Goals

| ID | Goal | Success Criteria |
|----|------|------------------|
| BG-05 | Developer experience'ı optimize etmek | Dev Services ile otomatik infrastructure provisioning |
| BG-06 | Production-ready architecture | Health monitoring, observability, scalability |
| BG-07 | Extensible platform | Custom pipeline ve model entegrasyonu desteklenmeli |

---

## 3. Functional Requirements

### 3.1 Document Processing

| ID | Requirement | Priority | Source |
|----|-------------|----------|--------|
| FR-001 | Sistem, HTTP URL'den doküman çekip işleyebilmeli | High | Article 1 |
| FR-002 | Sistem, local file system'den doküman yükleyebilmeli | High | Article 1 |
| FR-003 | Sistem, PDF dosyalarını parse edebilmeli | High | Article 1, 2 |
| FR-004 | Sistem, DOCX dosyalarını parse edebilmeli | Medium | Article 1 |
| FR-005 | Sistem, presentation dosyalarını parse edebilmeli | Medium | Article 1 |
| FR-006 | Sistem, dokümanları Markdown formatına dönüştürebilmeli | High | Article 1 |
| FR-007 | Sistem, tablo verilerini doğru şekilde extract edebilmeli | High | Article 1 |
| FR-008 | Sistem, OCR ile görüntülerden metin çıkarabilmeli | Medium | Article 1 |

### 3.2 RAG (Retrieval Augmented Generation)

| ID | Requirement | Priority | Source |
|----|-------------|----------|--------|
| FR-009 | Sistem, dokümanları chunk'lara ayırabilmeli | High | Article 2 |
| FR-010 | Sistem, chunk'ları vector embedding'lere dönüştürebilmeli | High | Article 2 |
| FR-011 | Sistem, embedding'leri vector store'da saklayabilmeli | High | Article 2 |
| FR-012 | Sistem, semantic search yapabilmeli | High | Article 2 |
| FR-013 | Sistem, kullanıcı sorularını doğal dilde kabul edebilmeli | High | Article 2 |
| FR-014 | Sistem, ilgili doküman context'i ile augmented yanıt üretebilmeli | High | Article 2 |

### 3.3 API & Integration

| ID | Requirement | Priority | Source |
|----|-------------|----------|--------|
| FR-015 | Sistem, REST API endpoint'leri sunmalı | High | Article 1, 2 |
| FR-016 | Sistem, health check endpoint'i sağlamalı | High | Article 1 |
| FR-017 | Sistem, Docling Serve API ile iletişim kurabilmeli | High | Article 1 |

### 3.4 Infrastructure

| ID | Requirement | Priority | Source |
|----|-------------|----------|--------|
| FR-018 | Development ortamında otomatik container provisioning yapılmalı | Medium | Article 1, 2 |
| FR-019 | Production ortamında external Docling Serve URL konfigüre edilebilmeli | High | Article 1 |

---

## 4. Scope Definition

### 4.1 In Scope

| Category | Items |
|----------|-------|
| **Document Processing** | PDF, DOCX, presentation parsing; Markdown conversion; table extraction; OCR |
| **RAG Pipeline** | Document ingestion; chunking; embedding; vector storage; semantic search; augmented generation |
| **API** | REST endpoints for document conversion and chat |
| **Infrastructure** | Dev Services integration; health monitoring; containerization |

### 4.2 Out of Scope

| Category | Items | Rationale |
|----------|-------|-----------|
| **UI/Frontend** | Web interface, mobile app | Makalelerde kapsam dışı |
| **Authentication** | User auth, authorization | Makalelerde belirtilmemiş |
| **Multi-tenancy** | Tenant isolation, data separation | Makalelerde belirtilmemiş |
| **Document Storage** | Long-term document archival | Sadece vector storage kapsam içi |
| **Model Training** | Custom model training | Pre-trained modeller kullanılacak |

---

## 5. Assumptions & Constraints

### 5.1 Assumptions

| ID | Assumption |
|----|------------|
| A-01 | Docker veya Podman runtime mevcut olacak (Dev Services için) |
| A-02 | OpenAI API key mevcut ve geçerli olacak |
| A-03 | Internet erişimi OpenAI API için sağlanacak |
| A-04 | PostgreSQL + PGVector production ortamında sağlanacak |
| A-05 | Yeterli disk alanı vector storage için mevcut olacak |
| A-06 | Network erişimi Docling Serve container'ına açık olacak |

### 5.2 Constraints

| ID | Constraint | Impact |
|----|------------|--------|
| C-01 | Java 25 gerekliliği | JDK 25+ kurulumu zorunlu |
| C-02 | Spring Boot 3.5+ gerekliliği | Framework version lock |
| C-03 | Embedding dimension 1536 sabit | OpenAI text-embedding-3-small modeline bağlı |
| C-04 | OpenAI API bağımlılığı | Internet bağlantısı ve API key gerekli |
| C-05 | MIT License compliance | Docling lisans şartlarına uyum |

### 5.3 Dependencies

| ID | Dependency | Version | Purpose |
|----|------------|---------|---------|
| D-01 | Spring Boot | 3.5+ | Application framework |
| D-02 | Spring AI | 1.1.2+ | AI/ML integration |
| D-03 | Arconia | 0.20.0+ | Docling Spring Boot integration |
| D-04 | Docling Serve | Latest | Document processing service |
| D-05 | OpenAI API | Latest | Cloud LLM inference (GPT-4o, text-embedding-3-small) |
| D-06 | PostgreSQL + PGVector | pg18 | Vector storage (1536 dimensions) |
| D-07 | Testcontainers | Latest | Dev Services container management |

---

## 6. Glossary (Initial Ubiquitous Language)

| Term | Definition | Context |
|------|------------|---------|
| **Docling** | IBM Research tarafından geliştirilen açık kaynak doküman parsing çözümü | Document Processing |
| **RAG** | Retrieval Augmented Generation - LLM yanıtlarını external knowledge ile zenginleştirme tekniği | AI/ML |
| **Chunk** | Bir dokümanın daha küçük, işlenebilir parçalara bölünmüş hali | Document Processing |
| **Embedding** | Metin verisinin sayısal vector temsili | AI/ML |
| **Vector Store** | Embedding'lerin depolandığı ve similarity search yapılan veritabanı | Infrastructure |
| **Semantic Search** | Anlam tabanlı benzerlik araması (keyword matching değil) | AI/ML |
| **Ingestion Pipeline** | Dokümanların yüklenmesi, işlenmesi ve saklanması süreci (ETL) | Document Processing |
| **DoclingDocument** | Docling'in unified output formatı, metadata kaybı olmadan tüm doküman türlerini temsil eder | Document Processing |
| **Dev Services** | Development ortamında otomatik infrastructure provisioning mekanizması | Infrastructure |
| **Advisor Pattern** | Spring AI'da request/response flow'unu modify eden pattern (RetrievalAugmentationAdvisor) | Architecture |
| **HNSW** | Hierarchical Navigable Small World - efficient semantic search için graph-based index algoritması | Infrastructure |
| **Hallucination** | LLM'in kaynak veride olmayan bilgiyi üretmesi | AI/ML |

---

## Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-23 | AI Assistant | Initial version based on article analysis |
| 1.1 | 2025-12-24 | AI Assistant | Switched from Ollama to OpenAI GPT-4o |