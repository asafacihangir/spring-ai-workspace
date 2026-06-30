# Project Context - Spring AI + Docling RAG System

> **Son Güncelleme:** 2025-12-25
> **Not:** Bu dosya kodlama stili icermez, sadece is ve proje gerceklerini icerir.

---

## 1. Projenin Amaci

Spring AI ve Docling teknolojilerini kullanarak kurumsal dokuman isleme ve Retrieval Augmented Generation (RAG) yetenekleri sunan bir uygulama gelistirmek.

### Cozdugu Problemler

| Problem | Aciklama | Cozum |
|---------|----------|-------|
| **Resource Intensity** | GenAI cozumleri yogun hesaplama kaynaklari gerektirir, on-premises deployment'i zorlastirir | Docling ile local dokuman isleme, OpenAI ile cloud LLM |
| **Privacy Concerns** | Cloud-based SaaS modelleri veri guvenligi ve gizlilik sorunlari yaratir | Dokuman parsing on-premises (Docling), sadece soru-cevap icin OpenAI |
| **Hallucination Risk** | Language model'ler kaynak dokumanlarda olmayan icerik uretebilir | RAG pattern ile context-aware yanitlar |

### Hedef Kullanicilar

| Kullanici | Kullanim Senaryosu |
|-----------|-------------------|
| **Gelistiriciler** | API entegrasyonu, dokuman isleme pipeline'lari olusturma |
| **Kurumsal Kullanicilar** | Dahili dokumanlar uzerinde soru-cevap sistemi |
| **DevOps Ekipleri** | Container-based deployment, health monitoring |

---

## 2. Is Hedefleri (Business Goals)

### Birincil Hedefler

| ID | Hedef | Basari Kriteri |
|----|-------|----------------|
| BG-01 | On-premises dokuman isleme yetenegini saglamak | Tum dokuman isleme local environment'ta gerceklesmeli |
| BG-02 | Dokuman isleme gizliligini korumak | Dokuman parsing on-premises gerceklesmeli |
| BG-03 | RAG tabanli bilgi erisimi sunmak | Kullanicilar dokumanlar hakkinda dogal dilde soru sorabilmeli |
| BG-04 | Coklu dokuman formati destegi | PDF, DOCX, presentation dosyalari islenebilmeli |

### Ikincil Hedefler

| ID | Hedef | Basari Kriteri |
|----|-------|----------------|
| BG-05 | Developer experience'i optimize etmek | Dev Services ile otomatik infrastructure provisioning |
| BG-06 | Production-ready architecture | Health monitoring, observability, scalability |
| BG-07 | Extensible platform | Custom pipeline ve model entegrasyonu desteklenmeli |

---

## 3. Kapsam (Scope)

### Kapsam Ici

| Kategori | Ozellikler |
|----------|------------|
| **Document Processing** | PDF, DOCX parsing; Markdown conversion; table extraction; OCR |
| **RAG Pipeline** | Document ingestion; chunking; embedding; vector storage; semantic search |
| **API** | REST endpoints (conversion, ingestion, chat) |
| **Infrastructure** | Dev Services; health monitoring; Docker containerization |

### Kapsam Disi

| Kategori | Aciklama |
|----------|----------|
| **UI/Frontend** | Web interface, mobile app |
| **Authentication** | User auth, authorization |
| **Multi-tenancy** | Tenant isolation, data separation |
| **Document Storage** | Long-term document archival |
| **Model Training** | Custom model training |

---

## 4. Teknoloji Stack'i

### Core Technologies

| Teknoloji | Versiyon | Amac |
|-----------|----------|------|
| **Java** | 25 | Programlama dili |
| **Spring Boot** | 3.5+ | Application framework |
| **Spring AI** | 1.1.2+ | AI/ML integration |
| **Maven** | 3.9+ | Build tool |

### Document Processing

| Teknoloji | Versiyon | Amac |
|-----------|----------|------|
| **Docling** | Latest | IBM Research dokuman parsing |
| **Docling Serve** | Latest | HTTP API wrapper |
| **Arconia** | 0.20.0+ | Spring Boot integration |

### AI/ML Infrastructure

| Teknoloji | Model | Amac |
|-----------|-------|------|
| **OpenAI API** | GPT-4o | Chat model for Q&A |
| **OpenAI API** | text-embedding-3-small | Embedding (1536 dimensions) |

### Data Storage

| Teknoloji | Versiyon | Amac |
|-----------|----------|------|
| **PostgreSQL** | 18 | Relational database |
| **PGVector** | Latest | Vector storage extension |

---

## 5. Mimari Kararlar (ADRs)

### ADR-001: Docling for Document Processing

**Karar:** Dokuman isleme icin Docling (IBM Research) kullanilacak.

**Gerekce:**
- MIT License, acik kaynak
- On-premises isleme, cloud bagimliligi yok
- Ozellestirilmis ML modelleri (layout, table, OCR)
- Spring Boot auto-configuration destegi

### ADR-002: OpenAI for LLM Inference

**Karar:** LLM inference icin OpenAI API (GPT-4o) kullanilacak.

**Gerekce:**
- State-of-the-art model kalitesi
- 1536 dimension embedding destegi
- Hizli inference, local GPU gerektirmez
- Guvenilir API

### ADR-003: PGVector for Vector Storage

**Karar:** Vector storage icin PostgreSQL + PGVector kullanilacak.

**Gerekce:**
- Taninmis PostgreSQL ekosistemi
- HNSW index ile hizli semantic search
- Spring AI ile iyi entegrasyon
- Production-ready ve olceklenebilir

### ADR-004: Controller Separation (SRP)

**Karar:** DocumentController, ConversionController ve IngestionController olarak ayrildi.

**Gerekce:**
- Single Responsibility Principle (SRP) uyumu
- Her controller tek bir is yapisi (conversion veya ingestion)
- Bakim kolayligi ve test edilebilirlik

### ADR-005: Text-Based Vector Search

**Karar:** Vector search icin text-based approach kullanilacak.

**Gerekce:**
- Spring AI VectorStore text-based search bekliyor
- Embedding generation VectorStore tarafindan yonetiliyor
- ProcessQueryUseCase basitlestirildi

---

## 6. Onemli Teknik Detaylar

### API Endpoints

| Method | Endpoint | Aciklama |
|--------|----------|----------|
| `GET` | `/api/documents/convert/http?url={url}` | URL'den dokuman donustur |
| `POST` | `/api/documents/convert` | Request body ile donustur |
| `GET` | `/api/documents/ingest/http?url={url}` | URL'den dokuman ingest et |
| `POST` | `/api/documents/ingest` | Request body ile ingest et |
| `POST` | `/api/chat` | RAG sorgusu gonder |
| `GET` | `/actuator/health` | Sistem saglik durumu |

### Environment Variables

| Degisken | Zorunlu | Varsayilan |
|----------|---------|------------|
| `OPENAI_API_KEY` | Evet | - |
| `POSTGRES_HOST` | Hayir | localhost |
| `POSTGRES_PORT` | Hayir | 15432 |
| `POSTGRES_DB` | Hayir | vectordb |
| `DOCLING_URL` | Hayir | http://localhost:5001 |

### Timeout Configuration

| Ayar | Deger | Aciklama |
|------|-------|----------|
| `arconia.docling.connect-timeout` | 10s | Baglanti timeout |
| `arconia.docling.read-timeout` | 10m | Okuma timeout (buyuk PDF'ler icin) |

---

## 7. Bilinen Kisitlamalar

| Kisitlama | Etki |
|-----------|------|
| Java 25 gerekliligi | JDK 25+ kurulumu zorunlu |
| Embedding dimension 1536 sabit | OpenAI modeline bagli |
| OpenAI API bagimliligi | Internet baglantisi ve API key gerekli |
| MIT License compliance | Docling lisans sartlarina uyum |

---

## 8. Performans Hedefleri

| Islem | Hedef | Maksimum |
|-------|-------|----------|
| Document Conversion (< 10 sayfa) | < 5 saniye | 15 saniye |
| Document Conversion (10-50 sayfa) | < 15 saniye | 45 saniye |
| RAG Query (tam yanit) | < 3 saniye | 10 saniye |
| Health Check | < 100 ms | 500 ms |

---

## 9. Varsayimlar

| ID | Varsayim |
|----|----------|
| A-01 | Docker veya Podman runtime mevcut olacak |
| A-02 | OpenAI API key mevcut ve gecerli olacak |
| A-03 | Internet erisimi OpenAI API icin saglanacak |
| A-04 | PostgreSQL + PGVector production ortaminda saglanacak |
| A-05 | Yeterli disk alani vector storage icin mevcut olacak |

---

## 10. Glossary (Ubiquitous Language)

| Terim | Tanim |
|-------|-------|
| **Docling** | IBM Research tarafindan gelistirilen acik kaynak dokuman parsing cozumu |
| **RAG** | Retrieval Augmented Generation - LLM yanitlarini external knowledge ile zenginlestirme teknigi |
| **Chunk** | Bir dokumanin daha kucuk, islenebilir parcalara bolunmus hali |
| **Embedding** | Metin verisinin sayisal vector temsili |
| **Vector Store** | Embedding'lerin depolandigi ve similarity search yapilan veritabani |
| **Semantic Search** | Anlam tabanli benzerlik aramasi |
| **Ingestion Pipeline** | Dokumanlarin yuklenmesi, islenmesi ve saklanmasi sureci |
| **Hallucination** | LLM'in kaynak veride olmayan bilgiyi uretmesi |

---

## 11. Iliskili Dokumanlar

| Dokuman | Aciklama |
|---------|----------|
| `docs/01-business_requirements/01-business-req.md` | Fonksiyonel gereksinimler |
| `docs/02-usecases/UC-*.md` | Use case dokumanlari |
| `docs/03-non_functional/01-nonfunc-req.md` | Non-functional requirements |
| `docs/architecture_rules.md` | Mimari kurallar |
| `docs/coding_standards.md` | Kodlama standartlari |
| `docs/nfr_techstack.md` | Technology stack & ADRs |
| `test-scenarios.md` | Manuel test senaryolari |

---

## Document History

| Versiyon | Tarih | Degisiklikler |
|----------|-------|---------------|
| 1.0 | 2025-12-25 | Ilk versiyon |
